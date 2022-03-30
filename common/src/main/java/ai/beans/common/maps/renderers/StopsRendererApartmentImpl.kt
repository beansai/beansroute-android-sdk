package ai.beans.common.maps.renderers

import ai.beans.common.MapClicked
import ai.beans.common.R
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.beanbusstation.BeansBusStation
import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.events.GoToNextStop
import ai.beans.common.events.GoToPreviousStop
import ai.beans.common.events.PinMoved
import ai.beans.common.events.ShowDataEntryDialog
import ai.beans.common.location.LocationHolder
import ai.beans.common.maps.BeansMapFragmentApartmentDetailsImpl
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
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class StopsRendererApartmentImpl(ownerFragment: BeansFragment, savedStateBundle: Bundle?)
    : BeansMapRenderer(ownerFragment, savedStateBundle),
    LifecycleObserver,
    PanelInteractionListener {

    var childList: ArrayList<RouteStop>? = null
    var routeStopsViewModel : RouteStopsViewModel? = null
    var locationHolder : LocationHolder?= null
    var mapFragment : BeansMapFragmentImpl? = null
    var totalBounds: ArrayList<GeoPoint>? = null
    var currentSelectedMarker : BeansMarkerInterface ?= null
    var allMarkersList = ArrayList<BeansMarkerInterface>()
    var unitMarkersList = ArrayList<BeansMarkerInterface>()
    var currentSelectedMarkerIndex = 0
    var totalDeliveredCount = 0
    var currentStop : RouteStop ?= null
    var beansInfoForStopMap = HashMap<String, SearchResponse?>()
    var beansNotesForStopMap = HashMap<String, NoteResponse?>()
    var parentStop : RouteStop ?= null
    var polygonArray = ArrayList<Polygon>()
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun setupObservers() {

        var multiStepObserver = MultiStateObserver()
        multiStepObserver?.setStateIds(arrayListOf<Int>(1,3))
        multiStepObserver?.setObserverFor(parentFragment.viewLifecycleOwner,
            routeStopsViewModel?.hasNewRoutesData!!, 1)

        multiStepObserver?.setObserverFor(parentFragment.viewLifecycleOwner, mapInterface?.getMapReadyFlag()!!, 3)

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
        //Get searchResults for child stop addresses
        //Get Notes for stop address
        //Render everything
        MainScope().launch {
            Log.d("Zoom_Level", "in updateViews")
            var isMapReady = mapInterface?.getMapReadyFlag()?.value
            isMapReady.let {
                var fragment = (parentFragment as BeansMapFragmentApartmentDetailsImpl)
                if (parentStop == null) {
                    refreshStopData()
                    getBeansSearchInfoForStopAddress(parentStop)
                    clearContent()
                    renderMapAssets()
                } else {
                    Log.d("DBG", "1")
                    refreshStopData()
                    Log.d("DBG", "5")
                    if (parentStop != null) {
                        if (!beansInfoForStopMap.containsKey(parentStop!!.list_item_id)) {
                            getBeansSearchInfoForStopAddress(parentStop)
                        }
                    }
                    clearContent()
                    renderMapAssets()
                }

                MainScope().launch {
                    (parentFragment as BeansMapFragmentApartmentDetailsImpl).containerPanel?.setPanelInteractionListener(
                        this@StopsRendererApartmentImpl
                    )
                }
            }
        }
    }

    private suspend fun refreshStopData() {
        var fragment = (parentFragment as BeansMapFragmentApartmentDetailsImpl)
        if (fragment.routeStopId != null) {
            parentStop = routeStopsViewModel?.getStopDetails(fragment.routeStopId!!)
            Log.d("DBG", "2")
        }
        Log.d("DBG", "3")
    }

    private suspend fun getBeansSearchInfoForStopAddress(stop : RouteStop?) {
        var fragment = (parentFragment as BeansMapFragmentApartmentDetailsImpl)
        if(stop != null ) {
            fragment.showHUD("Loading...")
            //Lets get all the child stop details
            childList = routeStopsViewModel?.getChildStopsForParent(parentStop!!.list_item_id!!)
            if(childList != null) {
                for (childStop in childList!!.iterator()) {
                    if(!beansInfoForStopMap.containsKey(childStop.list_item_id)) {
                        var fetchRoute = MainScope().async(Dispatchers.IO) {
                            var currLocation = locationHolder?.currentLocation
                            var center: GeoPoint? = null
                            if (currLocation != null) {
                                center = GeoPoint()
                                center.lat = currLocation?.latitude
                                center.lng = currLocation?.longitude
                            }

                            var response: Envelope<SearchResponse>? = null
                            response = getSearchResponse(childStop.address!!, childStop.unit, center)
                            if (response != null && response!!.success) {
                                beansInfoForStopMap[childStop.list_item_id!!] = response.data

                                var notesResponse = getAddressNotes(response.data!!.query_id!!)
                                if (notesResponse != null && notesResponse!!.success) {
                                    beansNotesForStopMap[childStop.list_item_id!!] =
                                        notesResponse.data
                                }
                            } else {
                                var x = SearchResponse()
                                var y = Route()
                                x.routes = ArrayList<Route>()
                                y.route_ui_data = RouteUiData()
                                y.route_ui_data?.markers = ArrayList<MarkerInfo>()
                                var z = MarkerInfo()
                                z.location = childStop.position
                                z.type = MapMarkerType.UNIT
                                z.status = "AVAILABLE"
                                z.route_point_type = MapMarkerType.UNIT
                                z.text = ""
                                y.route_ui_data?.markers?.add(z)
                                x.routes?.add(y)
                                beansInfoForStopMap[childStop.list_item_id!!] = x
                            }
                        }
                        fetchRoute.await()
                    }
                }
                fragment.hideHUD()
            }
        }
    }

    override fun clearContent() {
    }

    private fun clearMapAssets() {
        //Remove Polygons, PolyLines, Markers
        for(marker in allMarkersList) {
            mapInterface?.removeMarker(marker)
        }

        for(polygon in polygonArray) {
            polygon.remove()
        }
        allMarkersList.clear()
        unitMarkersList.clear()
        polygonArray.clear()
        currentSelectedMarker = null
        totalBounds = null
    }

    private suspend fun addApartmentStopMarkersToMap(stop: RouteStop) {
        if (mapInterface!!.isMapReady()) {
            if (parentFragment.context == null) {
                return
            }
            var markersFromServer = beansInfoForStopMap[stop.list_item_id]?.getMarkers()
            if (markersFromServer != null) {
                var iconGenerator = IconGenerator(parentFragment.context)
                for (beansMarkerInfo in markersFromServer) {
                    var geoPoint = beansMarkerInfo.location
                    if (geoPoint != null) {
                        if (beansMarkerInfo.status.equals("AVAILABLE")) {
                            var customIconData = customMarkerImagesViewModel?.getIconItemForType(beansMarkerInfo.type.toString())
                            if (customIconData != null) {
                                var bmp = beansMarkerInfo.getMarkerIconV2(
                                    iconGenerator,
                                    parentFragment.context!!,
                                    customIconData
                                )
                                if (bmp != null) {
                                    var markerAttributes = BeansMarkerAttributes()
                                    markerAttributes.bitmap = bmp
                                    markerAttributes.location = geoPoint
                                    markerAttributes.isVisible = true
                                    markerAttributes.isDraggable = true

                                    var marker = mapInterface?.createMarker(markerAttributes)

                                    var markerTag = HashMap<String, Any>()
                                    markerTag.put("stop", stop)
                                    markerTag.put("markerInfo", beansMarkerInfo)
                                    marker?.setMarkerTag(
                                        createMarkerTag(
                                            rendererId,
                                            markerTag
                                        ) as Object
                                    )
                                    allMarkersList.add(marker!!)
                                    if (beansMarkerInfo.type == MapMarkerType.UNIT) {
                                        unitMarkersList.add(marker)
                                    }
                                }
                                // XX marker?.showInfoWindow()
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateStopMarkerOnMap(marker: BeansMarkerInterface, selected : Boolean) {
        if(mapInterface!!.isMapReady()){
            //var iconGenerator = IconGenerator(parentFragment.context)
            var iconGenerator = IconGenerator(parentFragment.context)
            var markerTag = getDataFromTag(marker.getMarkerTag()!!, rendererId) as HashMap<String, Any>
            var stop = markerTag["stop"] as RouteStop?
            var markerInfo = markerTag["markerInfo"] as MarkerInfo

            var unitMarker = MarkerInfo()
            unitMarker.type = markerInfo!!.type
            unitMarker.text = stop!!.unit

            var customIconData = customMarkerImagesViewModel?.getIconItemForType(unitMarker.type.toString())
            if (customIconData != null) {
                MainScope().launch {
                    var bmp = unitMarker.getMarkerIconV2(
                        iconGenerator,
                        parentFragment.context!!,
                        customIconData
                    )
                    if (bmp != null) {
                        mapInterface?.updateMarkerIcon(marker, bmp!!)
                        centerMarker(marker)
                    }
                }
            }
        }

    }

    private fun centerMarker(marker: BeansMarkerInterface) {
        MainScope().launch {
            if(mapInterface!!.isMapReady()){
                var markerLocation = marker.getLocation()
                var loc = Location("")
                loc.latitude = markerLocation.lat!!
                loc.longitude = markerLocation.lng!!

                mapInterface?.setCurrentLocation(loc, null, true)
            }
        }
    }

    fun showNextMarker() {
        if(currentSelectedMarkerIndex + 1 >= unitMarkersList.size ) {
            currentSelectedMarkerIndex = 0
        } else {
            currentSelectedMarkerIndex++
        }
        var marker = unitMarkersList[currentSelectedMarkerIndex]
        handleMarkerClick(marker)
    }

    fun showPreviousMarker() {
        if(currentSelectedMarkerIndex - 1 < 0 ) {
            currentSelectedMarkerIndex = unitMarkersList.size - 1
        } else {
            currentSelectedMarkerIndex--
        }
        var marker = unitMarkersList[currentSelectedMarkerIndex]
        handleMarkerClick(marker)
    }

    fun handleMarkerClick(marker: BeansMarkerInterface) {
        if(mapInterface!!.isMapReady()){
            //Check if this is an apt stop and not the parent
            parentFragment as BeansMapFragmentApartmentDetailsImpl

            var markerTag = getDataFromTag(marker.getMarkerTag()!!, rendererId) as HashMap<String, Any>
            var stop = markerTag["stop"] as RouteStop?
            var markerInfo = markerTag["markerInfo"] as MarkerInfo?

            if(stop != null && markerInfo != null && markerInfo.type == MapMarkerType.UNIT) {
                //un-select current selection
                if(currentSelectedMarker != null) {
                    updateStopMarkerOnMap(currentSelectedMarker!!, false)
                }

                //make marker bigger
                updateStopMarkerOnMap(marker, true)
                currentSelectedMarker = marker

                currentStop = stop

                //Render the panel
                parentFragment.showCurrentPanel()
                parentFragment.containerPanel.hideMoreInfoButton = true
                parentFragment.containerPanel.showPrevNextButtons()
                parentFragment.containerPanel.renderStopCard(stop, false, true)
                parentFragment.containerPanel.removeChildRows()
                parentFragment.containerPanel.renderBeansNotes(
                    beansInfoForStopMap[stop.list_item_id],
                    beansNotesForStopMap[stop.list_item_id],
                    customMarkerImagesViewModel)

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : GoToNextStop) {
        showNextMarker()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : GoToPreviousStop) {
        showPreviousMarker()
    }

    override fun onMapMarkerClicked(marker: BeansMarkerInterface) {
        handleMarkerClick(marker)
    }

    override fun onMarkerDragEnd(marker: BeansMarkerInterface?) {
        var markerTag = getDataFromTag(marker!!.getMarkerTag()!!, rendererId) as HashMap<String, Any>
        var stop = markerTag["stop"] as RouteStop?
        var markerInfo = markerTag["markerInfo"] as MarkerInfo?

        var queryId = beansInfoForStopMap[stop!!.list_item_id]?.query_id

        //Confirm that user wants to move to this location...
        val builder = AlertDialog.Builder(parentFragment.context)
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which ->
                //User cancelled the drag!
                var oldPosition =
                    GeoPoint(markerInfo!!.location!!.lat!!, markerInfo.location!!.lng!!)
                marker.setLocation(oldPosition)
            })

        builder.setOnCancelListener {
            //User tapped outside to dismiss the alert....same as a cancel
            var oldPosition =
                GeoPoint(markerInfo!!.location!!.lat!!, markerInfo.location!!.lng!!)
            marker.setLocation(oldPosition)
        }

        builder.setPositiveButton(
            "Save",
            DialogInterface.OnClickListener { dialog, which ->
                //Save the new position
                saveNewMarkerLocation(marker, markerInfo!!, queryId, stop.list_item_id)
            })

        builder.setTitle("Save New Location")
        builder.setMessage("Are you sure you want to move this marker to a new location?")
        var dlg = builder.create()
        dlg.show()

        var button = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        with(button) {
            setTextColor(parentFragment.resources.getColor(R.color.colorPrimaryText))
        }

        button = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
        with(button) {
            setTextColor(parentFragment.resources.getColor(R.color.colorPrimaryText))
        }
    }

    fun onMarkerJumpEnd() {
        if (currentStop == null) {
            val builder = AlertDialog.Builder(parentFragment.context)
            builder.setNegativeButton("OK", DialogInterface.OnClickListener { dialog, which ->

            })

            builder.setTitle("Error")
            builder.setMessage("You must select a child stop to change position")
            var dlg = builder.create()
            dlg.show()

            var button = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
            with(button) {
                setTextColor(parentFragment.resources.getColor(R.color.colorBlack))
            }
            return
        }
        var currLocation = locationHolder?.currentLocation
        if (currLocation != null) {
            var center = GeoPoint()
            center.lat = currLocation?.latitude
            center.lng = currLocation?.longitude

            var marker = unitMarkersList[currentSelectedMarkerIndex]
            var markerTag = getDataFromTag(marker!!.getMarkerTag()!!, rendererId) as HashMap<String, Any>
            var stop = markerTag["stop"] as RouteStop?
            var markerInfo = markerTag["markerInfo"] as MarkerInfo?
            var queryId = beansInfoForStopMap[stop!!.list_item_id]?.query_id

            //Confirm that user wants to move to this location...
            val builder = AlertDialog.Builder(parentFragment.context)
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                //User cancelled the drag!
            })

            builder.setOnCancelListener {
                //User tapped outside to dismiss the alert....same as a cancel
            }

            builder.setPositiveButton("Save", DialogInterface.OnClickListener { dialog, which ->
                //Save the new position
                marker?.setLocation(center)
                mapInterface?.setCurrentLocation(currLocation, mapInterface?.getCurrentZoomLevel(), true)
                saveNewMarkerLocation(marker, markerInfo!!, queryId, stop.list_item_id)
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
    }

    private fun saveNewMarkerLocation(marker: BeansMarkerInterface, markerInfo: MarkerInfo, queryId: String?, listItemId : String) {
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

    override fun onMapClicked(location: LatLng) {
        renderParentStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : MapClicked) {
        //Unselect any current selected marker

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(note : NoteResponse) {
        MainScope().launch {
            beansNotesForStopMap[currentStop!!.list_item_id!!] = note
            var fragment = (parentFragment as BeansMapFragmentApartmentDetailsImpl)
            fragment.containerPanel.updateNotes(note)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : ShowDataEntryDialog) {
        MainScope().launch {
            //Check if we have a query id for this stop
            var queryId = beansInfoForStopMap[currentStop!!.list_item_id!!]?.query_id
            if(queryId != null) {
                val dlg = DataCollectorDialog()
                if (event.note != null) {
                    dlg.note = event.note
                }
                dlg.bus = BeansBusStation.BusStation.getBus(parentFragment.fragmentId!!)
                dlg.queryId = beansInfoForStopMap[currentStop!!.list_item_id!!]!!.query_id
                dlg.noteType = event.type
                dlg.show(parentFragment.fragmentManager!!, "notes")
            }
        }
    }


    override fun onPanelStateChanged(newState: Int) {
    }

    fun renderParentStop() {
        parentFragment as BeansMapFragmentApartmentDetailsImpl
        if(currentSelectedMarker != null) {
            updateStopMarkerOnMap(currentSelectedMarker!!, false)
            currentSelectedMarker = null
        }
        currentStop = null
        //Render the panel
        if(parentFragment.containerPanel.behaviour?.state == BottomSheetBehavior.STATE_HIDDEN) {
            parentFragment.showCurrentPanel()
        }
        parentFragment.containerPanel.hideMoreInfoButton = true
        parentFragment.containerPanel.renderStopCard(parentStop!!, true, true)
        if(parentStop!!.has_apartments) {
            parentFragment.containerPanel.renderChildStopsList(childList)
        }
        parentFragment.containerPanel.hideBeansNotes()
    }

    private suspend fun renderMapAssets() {
        var fragment = (parentFragment as BeansMapFragmentApartmentDetailsImpl)
        //We need to render the ploygons and markers for all the children (apts) for this stop
        if (mapInterface!!.isMapReady() && parentStop != null) {
            clearMapAssets()
            var points = ArrayList<LatLng>()
            //We need to show polygons using data from the "search" call and not what comes via the route stop
            showAptUnitsPolygons(childList)

            if (childList != null && childList!!.isNotEmpty()) {
                totalDeliveredCount = 0
                for (aptStop in childList!!) {
                    //Add ther apartment polygon
                    var pointArray = aptStop?.polygon?.point
                    if (pointArray != null) {
                        for (point in pointArray) {
                            points.add(LatLng(point.lat!!, point.lng!!))
                        }
                    }
                    //Add the apt location into point array...just in case
                    points.add(
                        LatLng(
                            aptStop.display_position!!.lat!!,
                            aptStop.display_position!!.lng!!
                        )
                    )
                    addApartmentStopMarkersToMap(aptStop)

                    var markersFromServer = beansInfoForStopMap[aptStop.list_item_id]?.getMarkers()
                    if (markersFromServer != null) {
                        for (beansMarkerInfo in markersFromServer) {
                            var geoPoint = beansMarkerInfo.location
                            if (geoPoint != null) {
                                points.add(LatLng(
                                    geoPoint.lat!!,
                                    geoPoint.lng!!
                                ))
                            }
                        }
                    }

                    //update the delivered count if this apt stop is done
                    if(aptStop.status == RouteStopStatus.FINISHED ||  aptStop.status == RouteStopStatus.FAILED) {
                        totalDeliveredCount++
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
                mapInterface?.setCurrentBounds(totalBounds!!, 500, 500, 200)
            }

            //finally, which stop do we show on the card?
            if(currentStop == null) {
                //Show the parent
                renderParentStop()
            } else {
                var marker = unitMarkersList[currentSelectedMarkerIndex]
                handleMarkerClick(marker)
            }

            //We render stops (parent+children) anytime the model for any changes...
            //Check if the parent status is set to complete...that means this apt stop is
            //done so we need to set "currentActiveStop to the next stop (if there is an optimized
            //set of stops. This way when user gets back to the map, they will see this next stop
            if(parentStop!!.status == RouteStopStatus.FINISHED) {
                var currentStop = routeStopsViewModel?.getCurrentActiveStop()
                if(currentStop != null
                    && currentStop.list_item_id.equals(parentStop!!.list_item_id))
                routeStopsViewModel?.moveToNextStop()
            }
        }
    }

    private fun showAptUnitsPolygons(units: ArrayList<RouteStop>?) {
        /*TODO: var mapView = getMapView()
        if(units != null) {
            for (unit in units) {
                var polygonOptions = PolygonOptions()
                if(unit.polygon != null && unit.polygon!!.point != null) {
                    for (point in unit.polygon!!.point!!) {
                        polygonOptions.add(LatLng(point.lat!!, point.lng!!))
                    }
                    polygonOptions.strokeColor(parentFragment.resources.getColor(R.color.polygon_outline))
                    polygonOptions.strokeWidth(3.0f)
                    polygonOptions.fillColor(parentFragment.resources.getColor(R.color.polygon_fill))

                    var polygon = mapView?.value?.renderPolygon(polygonOptions)
                    if(polygon != null) {
                        polygonArray.add(polygon)
                    }
                }
            }
        }*/
    }
}