package ai.beans.common.maps.renderers

import ai.beans.common.R
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.beanbusstation.BeansBusStation
import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.events.PinMoved
import ai.beans.common.events.ShowDataEntryDialog
import ai.beans.common.location.LocationHolder
import ai.beans.common.maps.BeansMapFragmentAddressDetailsImpl
import ai.beans.common.maps.BeansMapFragmentImpl
import ai.beans.common.maps.createMarkerTag
import ai.beans.common.maps.getDataFromTag
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.networking.Envelope
import ai.beans.common.networking.isp.getAddressNotes
import ai.beans.common.networking.isp.getSearchResponse
import ai.beans.common.networking.isp.postNewLocationForPrimaryMarker
import ai.beans.common.panels.PanelInteractionListener
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.pojo.RouteStop
import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.search.*
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.utils.MultiStateObserver
import ai.beans.common.viewmodels.RouteStopsViewModel
import ai.beans.common.widgets.DataCollectorDialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class StopsRendererSingleImpl (ownerFragment: BeansFragment, savedStateBundle: Bundle?)
    : BeansMapRenderer(ownerFragment, savedStateBundle),
    LifecycleObserver, PanelInteractionListener {
    var buildingPolygon: Polygon? = null
    var routeStopsViewModel : RouteStopsViewModel? = null
    var locationHolder : LocationHolder? = null
    var mapFragment : BeansMapFragmentImpl? = null
    var totalBounds: ArrayList<GeoPoint>? = null
    var routeStop : RouteStop? = null
    var stopBeansMarkerInfo : SearchResponse ? = null
    var stopNotesInfo : NoteResponse ? = null
    var markersMap = HashMap<String, BeansMarkerInterface>()
    var isDataRendered = false
    var customMarkerImagesViewModel: CustomMarkerImagesViewModel? = null
    var cachedPinMovement: CachedPinMovement? = null

    init {
        routeStopsViewModel = ViewModelProviders.of(parentFragment.activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            RouteStopsViewModel::class.java)

        locationHolder = ViewModelProviders.of(parentFragment.activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            LocationHolder::class.java)

        customMarkerImagesViewModel = ViewModelProviders.of(parentFragment.activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            CustomMarkerImagesViewModel::class.java)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun registerEventBus() {
        restoreState()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun setListeners() {
        //mapFragment.containerPanel?.setPanelInteractionListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterEventBus() {
        if (cachedPinMovement != null) {
            EventBus.getDefault().post(
                PinMoved(
                    cachedPinMovement!!.listItemId,
                    cachedPinMovement!!.lat,
                    cachedPinMovement!!.lng,
                    cachedPinMovement!!.type
                )
            )
            cachedPinMovement = null
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPauseEvent() {
        isDataRendered = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun setupObservers() {

        var multiStepObserver = MultiStateObserver()
        multiStepObserver?.setStateIds(arrayListOf<Int>(1,3))
        multiStepObserver?.setObserverFor(parentFragment.viewLifecycleOwner,
            routeStopsViewModel?.hasNewRoutesData!!, 1)

        multiStepObserver?.setObserverFor(parentFragment.viewLifecycleOwner, mapInterface!!.getMapReadyFlag(), 3)

        multiStepObserver?.multiStateIsReady?.observe(parentFragment.viewLifecycleOwner, Observer{
            if(parentFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                Log.d("ROUTE_STOP", "in MSO Observer")
                updateViews()
            }
        })

    }

    fun saveState(outState : Bundle) {
    }

    fun restoreState() {
    }

    fun updateViews() {
        //Check if we have the stop to render...if not get from Db
        //Get searchResults for stop address
        //Get Notes for stop address
        //Render everything
        MainScope().launch {
            //We always get the latest stop data
            refreshStopData()
            //The beans search...we dont make everytime
            if(stopBeansMarkerInfo == null) {
                getBeansSearchInfoForStopAddress(routeStop)
            }
            renderAddressDetails()
            //We render stuff on the map only if we have not yet
            if(!isDataRendered) {
                isDataRendered = true
                clearContent()
                renderRoute()
            }
        }
    }

    private fun refreshStopData() {
        var fragment = (parentFragment as BeansMapFragmentAddressDetailsImpl)
        Log.d("DBG", fragment.routeStopId!!)
        if (fragment.routeStopId != null) {
            routeStop = routeStopsViewModel?.getStopDetails(fragment.routeStopId!!)
            Log.d("DBG", "2")
        }
        Log.d("DBG", "3")
    }

    private suspend fun getBeansSearchInfoForStopAddress(stop : RouteStop?) {
        var fragment = (parentFragment as BeansMapFragmentAddressDetailsImpl)
        if(stop != null ) {
            fragment.showHUD("Loading...")
            var fetchRoute = MainScope().async(Dispatchers.IO) {
                var currLocation = locationHolder?.currentLocation
                var center: GeoPoint? = null
                if (currLocation != null) {
                    center = GeoPoint()
                    center.lat = currLocation?.latitude
                    center.lng = currLocation?.longitude
                }

                var response: Envelope<SearchResponse>? = null
                response = getSearchResponse(stop!!.address!!, stop!!.unit, center)
                if (response != null && response!!.success) {
                    stopBeansMarkerInfo = response.data
                    var notesResponse = getAddressNotes(response.data!!.query_id!!)
                    if (notesResponse != null && notesResponse!!.success) {
                        stopNotesInfo = notesResponse.data
                    }
                } else {
                    var x = SearchResponse()
                    var y = Route()
                    x.routes = ArrayList<Route>()
                    y.route_ui_data = RouteUiData()
                    y.route_ui_data?.markers = ArrayList<MarkerInfo>()
                    y.route_ui_data?.navigate_to = stop.position
                    var z = MarkerInfo()
                    z.location = stop.position
                    z.type = MapMarkerType.UNIT
                    z.status = "AVAILABLE"
                    z.route_point_type = MapMarkerType.UNIT
                    z.text = ""
                    y.route_ui_data?.markers?.add(z)
                    x.routes?.add(y)
                    stopBeansMarkerInfo = x
                }
            }
            fetchRoute.await()
            fragment.hideHUD()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : ShowDataEntryDialog) {
        MainScope().launch {
            val dlg = DataCollectorDialog()
            if(event.note!=null)
            {
                dlg.note=event.note
            }
            dlg.bus = BeansBusStation.BusStation.getBus(parentFragment.fragmentId!!)
            dlg.queryId = stopBeansMarkerInfo?.query_id
            dlg.noteType = event.type
            dlg.show(parentFragment.fragmentManager!!, "notes")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(note : NoteResponse) {

        MainScope().launch {
            stopNotesInfo = note
            var fragment = (parentFragment as BeansMapFragmentAddressDetailsImpl)
            fragment.containerPanel.updateNotes(note)
        }
    }


    override fun onMapMarkerClicked(marker: BeansMarkerInterface) {
        //var mapView = getMapView()
        if(mapInterface!!.isMapReady()) {
            //center the marker....
            //..and bring up the panel
            MainScope().launch {
                renderAddressDetails()
            }
        }
    }

    override fun onMarkerDragEnd(marker: BeansMarkerInterface?) {
        //Confirm that user wants to move to this location...
        val builder = AlertDialog.Builder(parentFragment.context)
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            //User cancelled the drag!
            var markerInfo = getDataFromTag(marker?.getMarkerTag()!!, rendererId) as MarkerInfo
            var oldPosition = GeoPoint(markerInfo.location!!.lat!!, markerInfo.location!!.lng!!)
            marker.setLocation(oldPosition)
        })

        builder.setOnCancelListener {
            //User tapped outside to dismiss the alert....same as a cancel
            var markerInfo = getDataFromTag(marker?.getMarkerTag()!!, rendererId) as MarkerInfo
            var oldPosition = GeoPoint(markerInfo.location!!.lat!!, markerInfo.location!!.lng!!)
            marker.setLocation(oldPosition)
        }

        builder.setPositiveButton("Save", DialogInterface.OnClickListener { dialog, which ->
            //Save the new position
            saveNewMarkerLocation(marker, stopBeansMarkerInfo?.query_id, routeStop!!.list_item_id!!)
        })

        builder.setTitle("Save New Location")
        builder.setMessage("Are you sure you want to move this marker to a new location?")
        var dlg = builder.create()
        dlg.show()

        var button = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        with(button) {
            setTextColor(parentFragment.resources.getColor(R.color.colorBlack))
        }

        button = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
        with(button) {
            setTextColor(parentFragment.resources.getColor(R.color.colorBlack))
        }
    }

    private fun saveNewMarkerLocation(marker: BeansMarkerInterface?, queryId: String?, listItemId: String) {
        var markerInfo = getDataFromTag(marker?.getMarkerTag()!!, rendererId) as MarkerInfo
        var newLocation = LocationUpdateRequest()
        newLocation.type = markerInfo.type
        var newMarkerPos = marker.getLocation()
        newLocation.point = GeoPoint()
        newLocation.point!!.lat = newMarkerPos.lat
        newLocation.point!!.lng = newMarkerPos.lng
        var newLocationsArray = ArrayList<LocationUpdateRequest>()
        newLocationsArray.add(newLocation)

        var hashMap = HashMap<String, ArrayList<LocationUpdateRequest>>()
        hashMap.put("items", newLocationsArray)

        //fire and forget
        if (queryId != null) {
            MainScope().launch(Dispatchers.IO) {
                postNewLocationForPrimaryMarker(hashMap, queryId, listItemId)
            }
        }

        if (markerInfo.type == MapMarkerType.PARKING || markerInfo.type == MapMarkerType.UNIT) {
            cachedPinMovement = CachedPinMovement(
                listItemId,
                newMarkerPos.lat,
                newMarkerPos.lng,
                markerInfo.type
            )
        }
    }

    override fun onPanelStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
        }
    }

    fun renderAddressDetails() {
        parentFragment as BeansMapFragmentAddressDetailsImpl
        if(stopBeansMarkerInfo != null) {
            //Render the panel
            refreshStopData()
            parentFragment.showCurrentPanel()
            parentFragment.containerPanel.hideMoreInfoButton = true
            parentFragment.containerPanel.renderStopCard(routeStop!!)
            parentFragment.containerPanel.renderBeansNotes(
                stopBeansMarkerInfo!!,
                stopNotesInfo,
                customMarkerImagesViewModel)
        }
    }

    private suspend fun renderRoute() {
        var fragment = (parentFragment as BeansMapFragmentAddressDetailsImpl)
        if (stopBeansMarkerInfo != null) {
            if (mapInterface!!.isMapReady()) {
                clearContent()
                var points = ArrayList<LatLng>()
                var pointsFromServer = stopBeansMarkerInfo?.getPolygonPoints()
                if (pointsFromServer != null) {
                    for (point in pointsFromServer) {
                        points.add(LatLng(point.lat!!, point.lng!!))
                    }
                }

                var markersFromServer = stopBeansMarkerInfo?.getMarkers()
                if (markersFromServer != null) {
                    for (marker in markersFromServer) {
                        if (marker.status.equals("AVAILABLE")) {
                            var geoPoint = marker.location
                            if (geoPoint != null) {
                                points.add(LatLng(geoPoint.lat!!, geoPoint.lng!!))
                            }
                        }
                    }
                }

                for (latLng in points) {
                    var point = GeoPoint()
                    point.lat = latLng.latitude
                    point.lng = latLng.longitude

                    if (totalBounds == null) {
                        totalBounds = ArrayList<GeoPoint>()
                        totalBounds!!.add(point)
                    } else {
                        totalBounds!!.add(point)
                    }
                }

                //We got the bounds
                totalBounds?.let {
                    mapInterface?.setCurrentBounds(it, 500, 500, 200)
                    //generateLinkDisplayAssets(routes)
                }
                showAddressDetailsPolygon(stopBeansMarkerInfo)
                renderMarkers(stopBeansMarkerInfo)
            }

            //We render stops (parent+children) anytime the model for any changes...
            //Check if the parent status is set to complete...that means this apt stop is
            //done so we need to set "currentActiveStop to the next stop (if there is an optimized
            //set of stops. This way when user gets back to the map, they will see this next stop
            if(routeStop!!.status != RouteStopStatus.NEW) {
                if(routeStopsViewModel?.getCurrentActiveStop() != null
                    && routeStopsViewModel?.getCurrentActiveStop()!!.list_item_id.equals(routeStop!!.list_item_id))
                    routeStopsViewModel?.moveToNextStop()
            }

        }
    }

    private fun showAddressDetailsPolygon(routes: SearchResponse?) {
        //ToDo: Can we not re-iterate over this array again? We did it in the
        //ToDo: calling method
        /*var mapView = getMapView()?.value
        if(routes?.routes?.get(0)!!.route_ui_data?.building_shape != null) {
        var mapView = getMapView()?.value
        if(parentFragment.isAdded && routes?.routes?.get(0)!!.route_ui_data?.building_shape != null) {
            var polygonOptions = PolygonOptions()
            for (point in routes?.routes?.get(0)!!.route_ui_data?.building_shape!!.point!!) {
                polygonOptions.add(LatLng(point.lat!!, point.lng!!))
            }
            polygonOptions.strokeColor(parentFragment.resources.getColor(R.color.polygon_outline))
            polygonOptions.strokeWidth(3.0f)
            polygonOptions.fillColor(parentFragment.resources.getColor(R.color.polygon_fill))

            buildingPolygon = mapView?.renderPolygon(polygonOptions)
        }*/
    }

    private suspend fun renderMarkers(routes: SearchResponse?) {
        if (mapInterface!!.isMapReady()) {
            if (parentFragment.context == null) {
                return
            }
            if (routes != null) {
                var markersFromServer = routes?.getMarkers()
                if (markersFromServer != null) {

                    var iconGenerator = IconGenerator(parentFragment.context)
                    for (beansMarkerInfo in markersFromServer) {
                        var geoPoint = beansMarkerInfo.location
                        if (geoPoint != null) {
                            if (beansMarkerInfo.status.equals("AVAILABLE")) {
                                //beansMarkerMap[beansMarkerInfo.id] = beansMarkerInfo

                                var customIconData = customMarkerImagesViewModel?.getIconItemForType(beansMarkerInfo.type.toString())
                                if(customIconData != null) {
                                    var bmp = beansMarkerInfo.getMarkerIconV2(
                                        iconGenerator,
                                        parentFragment.context!!,
                                        customIconData
                                    )
                                    if (bmp != null) {
                                        var markerAttrib = BeansMarkerAttributes()
                                        markerAttrib.bitmap = bmp
                                        markerAttrib.isVisible = true
                                        markerAttrib.location = geoPoint
                                        markerAttrib.isDraggable = true

                                        val marker = mapInterface!!.createMarker(markerAttrib)
                                        marker?.setMarkerTag(
                                            createMarkerTag(
                                                rendererId,
                                                beansMarkerInfo
                                            ) as Object
                                        )
                                        markersMap.put(beansMarkerInfo.id.toString(), marker!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun clearContent() {
        //Clear Markers
        for(marker in markersMap.values) {
            mapInterface?.removeMarker(marker)
        }
        //CLear building polygon
        buildingPolygon?.remove()

        markersMap.clear()
        buildingPolygon = null
    }
}