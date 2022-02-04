package ai.beans.common.maps.renderers

import ai.beans.common.MapMoved
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.events.*
import ai.beans.common.maps.BeansMapFragmentRouteViewImpl
import ai.beans.common.maps.createMarkerTag
import ai.beans.common.maps.getDataFromTag
import ai.beans.common.maps.mapproviders.BeansMapInterface
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.maps.polylines.BeansPolyline
import ai.beans.common.panels.PanelInteractionListener
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.pojo.RouteStop
import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.utils.MultiStateObserver
import ai.beans.common.viewmodels.CurrentActiveRouteStopViewModel
import ai.beans.common.viewmodels.RouteStopsViewModel
import ai.beans.common.widgets.markers.IconAttributes
import ai.beans.common.widgets.markers.MarkerIconHelper
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class StopsRendererRouteImpl(ownerFragment: BeansFragment, savedStateBundle: Bundle?) :
    BeansMapRenderer(ownerFragment, savedStateBundle),
    LifecycleObserver,
    PanelInteractionListener {
    var routeDataViewModel: RouteStopsViewModel? = null
    var currentStopViewModel: CurrentActiveRouteStopViewModel? = null
    var routeStopMarkers = HashMap<String, BeansMarkerInterface>()
    var routeStopMarkersWithText = HashMap<String, BeansMarkerInterface>()

    //Current Selected Marker/Stop
    var currentSelectedStopId: String? = null

    //Current Selected Marker/Stop during optimization
    val TRANSPARENT_DRAWABLE = ColorDrawable(Color.TRANSPARENT)
    var savedZoom: Float? = null
    var savedLocation: LatLng? = null
    var mapFragment: BeansMapInterface? = null
    val polylineArray = ArrayList<BeansPolyline>()
    var projection: Projection? = null
    var showingCompletedMarkersFeature = true
    var showMarkersWithAddressFeature = false
    var showMarkersWithSidColor = false
    var showingAddressOnMarkers = false
    var mapReadyAndMapDataReady: MultiStateObserver? = null
    var mapAndMarkersReadyAndNewMarkerSelected: MultiStateObserver? = null
    var readyToRenderMarkers: MultiStateObserver? = null

    var isRenderRouteComplete: MutableLiveData<Boolean>? = null

    init {
        routeDataViewModel = ViewModelProviders.of(
            ownerFragment.activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)
        ).get(RouteStopsViewModel::class.java)

        currentStopViewModel = ViewModelProviders.of(
            ownerFragment.activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)
        ).get(CurrentActiveRouteStopViewModel::class.java)

        iconGenerator = IconGenerator(ownerFragment.context)
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
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        currentSelectedStopId = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun setupObservers() {
        isRenderRouteComplete = MutableLiveData()

        mapReadyAndMapDataReady = MultiStateObserver()
        mapReadyAndMapDataReady?.setStateIds(arrayListOf<Int>(1, 3))

        mapReadyAndMapDataReady?.setObserverFor(
            parentFragment.viewLifecycleOwner,
            routeDataViewModel?.hasNewRoutesData!!, 1
        )

        mapReadyAndMapDataReady?.setObserverFor(
            parentFragment.viewLifecycleOwner,
            mapInterface!!.getMapReadyFlag(),
            3
        )

        mapReadyAndMapDataReady?.multiStateIsReady?.observe(
            parentFragment.viewLifecycleOwner,
            Observer {
                if (parentFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d("ROUTE_STOP", "in MSO Observer")
                    updateViews()
                }
            })

        readyToRenderMarkers = MultiStateObserver()
        readyToRenderMarkers?.setStateIds(arrayListOf<Int>(1))
        readyToRenderMarkers?.setObserverFor(
            parentFragment.viewLifecycleOwner,
            mapReadyAndMapDataReady?.multiStateIsReady!!,
            1
        )
        readyToRenderMarkers?.multiStateIsReady?.observe(
            parentFragment.viewLifecycleOwner,
            Observer {
                if (parentFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d("ROUTE_STOP", "in MSO Observer")
                    updateViews()
                }
            })

        mapAndMarkersReadyAndNewMarkerSelected = MultiStateObserver()
        mapAndMarkersReadyAndNewMarkerSelected?.setStateIds(arrayListOf<Int>(1, 2))
        mapAndMarkersReadyAndNewMarkerSelected?.setObserverFor(
            parentFragment.viewLifecycleOwner,
            routeDataViewModel?.hasNewSelectedStop!!,
            1
        )
        mapAndMarkersReadyAndNewMarkerSelected?.setObserverFor(
            parentFragment.viewLifecycleOwner,
            isRenderRouteComplete!!,
            2
        )
        mapAndMarkersReadyAndNewMarkerSelected?.multiStateIsReady?.observe(
            parentFragment.viewLifecycleOwner,
            Observer {
                if (parentFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d("ROUTE_STOP", "in NEW STOP MSO Observer")
                    handleNewMarkerSelection()
                }
            })

        routeDataViewModel?.hasNewRoutePath?.observe(
            parentFragment.viewLifecycleOwner,
            Observer {
                if (parentFragment.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    updatePaths()
                }
            })
    }

    private fun setTitle() {
    }

    private fun handleNewMarkerSelection() {
        MainScope().launch {
            Log.d("ROUTE_STOP", "handleNewMarkerSelection")
            //Is there a previous selection?
            if (currentSelectedStopId != null) {
                //Update both markers (with text/without to the smaller version
                var currentMarker = routeStopMarkers[currentSelectedStopId!!]
                var stop = getDataFromTag(currentMarker!!.getMarkerTag()!!, rendererId) as RouteStop

                var attributes = IconAttributes()

                attributes.addressString = getMarkerText(stop)
                attributes.hasApartments = stop.has_apartments
                //XX attributes.number = routeDataViewModel?.getStopLabel(stop)
                attributes.number = stop.route_display_number
                attributes.status = stop.status
                attributes.type = stop.type
                attributes.sid = stop.tracking_id
                attributes.useSidColors = showMarkersWithSidColor

                updateRouteStopMarkerIcon(currentMarker!!, stop, attributes)

                currentMarker = routeStopMarkersWithText[currentSelectedStopId!!]
                attributes.showMarkerWithTextLabel = true

                updateRouteStopMarkerIcon(currentMarker!!, stop, attributes)
            }

            var currentStop = routeDataViewModel?.getCurrentStop()
            if (currentStop != null) {
                var attributes = IconAttributes()

                attributes.addressString = getMarkerText(currentStop)
                attributes.hasApartments = currentStop.has_apartments
                //XX attributes.number = routeDataViewModel?.getStopLabel(currentStop)
                attributes.number = currentStop.route_display_number
                attributes.status = currentStop.status
                attributes.type = currentStop.type
                attributes.showSelected = true
                attributes.sid = currentStop.tracking_id
                attributes.useSidColors = showMarkersWithSidColor

                var marker = routeStopMarkers[currentStop.list_item_id]
                if (marker != null) {
                    currentSelectedStopId = currentStop.list_item_id

                    updateRouteStopMarkerIcon(marker!!, currentStop, attributes)

                    MainScope().launch {
                        (parentFragment as BeansMapFragmentRouteViewImpl).stopCardPanelImpl?.renderStopCard(
                            currentStop
                        )
                    }
                } else {
                    (parentFragment as BeansMapFragmentRouteViewImpl).stopCardPanelImpl?.hide()
                    currentSelectedStopId = null
                }
                //marker with address
                marker = routeStopMarkersWithText[currentStop.list_item_id]
                attributes.showMarkerWithTextLabel = true
                if (marker != null) {
                    currentSelectedStopId = currentStop.list_item_id
                    updateRouteStopMarkerIcon(marker!!, currentStop, attributes)
                }
            } else {
                //there is no marker selected....hide the panel
                (parentFragment as BeansMapFragmentRouteViewImpl).stopCardPanelImpl?.hide()
                currentSelectedStopId = null
            }
        }
    }

    private fun getMarkerText(stop: RouteStop): String? {
        var addressString = stop.address_components?.street
        if (stop.tracking_id != null) {
            addressString = addressString + " SID: " + stop.tracking_id
        }
        return addressString
    }

    private fun updatePaths() {
        if (mapInterface!!.isMapReady()) {
            var paths = routeDataViewModel?.pathSegments
            if (paths != null && paths.line != null) {
                var polylinePoints = ArrayList<GeoPoint>()
                for (pathArray in paths.line!!) {
                    if (pathArray.point != null) {
                        for (segment in pathArray.point!!) {
                            polylinePoints.add(segment)
                        }
                    }
                }
                var polyline = mapInterface!!.createPolyline(polylinePoints)
                hidePolyLines()
                polylineArray.clear()
                if (polyline != null) {
                    polylineArray.add(polyline)
                }
            }
        }
    }

    private fun hidePolyLines() {
        for (line in polylineArray) {
            mapInterface?.hidePolyline(line)
        }
    }

    private fun showPolyLines() {
        for (line in polylineArray) {
            mapInterface?.showPolyline(line)
        }
    }

    private fun showAllMarkersWithoutRouteNumbers() {
    }

    fun saveState(outState: Bundle) {
    }

    fun restoreState() {
    }

    private fun updateViews() {
        Log.d("SYNC", "updateViews outside")
        MainScope().launch {
            routeDataViewModel?.mutex!!.withLock {
                var allStops: ArrayList<RouteStop>? = null
                allStops = routeDataViewModel?.allStops
                renderRouteStops(allStops!!)
                updatePaths()

                MainScope().launch {
                    (parentFragment as BeansMapFragmentRouteViewImpl).stopCardPanelImpl?.setPanelInteractionListener(
                        this@StopsRendererRouteImpl
                    )
                }
            }
        }
    }

    private fun renderRouteStops(routeStops: ArrayList<RouteStop>) {
        //routeStopMarkers.clear()
        //routeStopMarkersWithText.clear()
        clearContent()
        currentSelectedStopId = null
        //var mapView = getMapView()?.value
        if (mapInterface!!.isMapReady()) {
            //mapView.getMap()?.clear()
            if (routeStops != null && !routeStops.isEmpty()) {
                var points = ArrayList<GeoPoint>()
                var count = 1
                for (stop in routeStops) {
                    Log.d("SYNC", "in render loop")
                    points.add(GeoPoint(stop.display_position?.lat!!, stop.display_position?.lng!!))
                    //get a marker
                    var showBigMarker = false
                    setRouteStopMarkerIcon(
                        stop.route_display_number,
                        stop,
                        showBigMarker
                    )
                    count++
                }

                //We have created all the markers...populatd the hashmaps..
                //Now we render the ones that need to be shown
                switchMarkersOnMap()

                var totalBounds = ArrayList<GeoPoint>()
                for (point in points) {
                    totalBounds!!.add(point)
                }

                if (savedLocation == null) {
                    totalBounds?.let {
                        mapInterface?.setCurrentBounds(it, 1080, 1080, 80)
                        savedLocation = LatLng(1.0, 1.0)
                    }
                } else {

                }
                Log.d("SYNC", "Rendering Complete")
                isRenderRouteComplete?.value = true
            } else {
                savedLocation = null
            }
        }
    }


    private fun centerMarker(marker: BeansMarkerInterface) {
        MainScope().launch {
            if (mapInterface!!.isMapReady()) {
                var location = Location("")
                var markerLoc = marker.getLocation()
                location.latitude = markerLoc.lat!!
                location.longitude = markerLoc.lng!!
                mapInterface?.setCurrentLocation(location, null, true)
            }
        }
    }

    private fun setRouteStopMarkerIcon(
        label: Int? = null,
        stop: RouteStop,
        showSelected: Boolean = false,
        showAddressOnMarker: Boolean = false
    ) {
        iconGenerator?.setBackground(TRANSPARENT_DRAWABLE)

        //We create 2 markers for each stop.
        //One has text (address) and one does not
        //we add both to the map but one is hidden

        var attributes = IconAttributes()
        attributes.status = stop.status
        attributes.type = stop.type
        attributes.hasApartments = stop.has_apartments
        attributes.showSelected = showSelected
        attributes.number = label
        attributes.addressString = getMarkerText(stop)
        attributes.sid = stop.tracking_id
        attributes.useSidColors = showMarkersWithSidColor

        //bitmap without address...
        var bitmap = MarkerIconHelper().getMarkerIconBitmap(
            parentFragment.context!!,
            iconGenerator!!,
            attributes
        )
        //bitmap with address
        attributes.showMarkerWithTextLabel = true
        var bitmapWithAddress = MarkerIconHelper().getMarkerIconBitmap(
            parentFragment.context!!,
            iconGenerator!!,
            attributes
        )

        //Add  marker without address
        var zIndex = if (showSelected) 100.0f; else 90.0f
        /*var icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        var markerOptions = MarkerOptions().position(LatLng(stop.display_position?.lat!!, stop.display_position?.lng!!))
            .icon(icon)
            .visible(false)
            .zIndex(zIndex)
            .anchor(0.5f, 1.0f)
            //.alpha(0.7f)*/

        var markerAttributes = BeansMarkerAttributes()
        markerAttributes.bitmap = bitmap
        markerAttributes.location =
            GeoPoint(stop.display_position?.lat!!, stop.display_position?.lng!!)
        markerAttributes.isVisible = false
        //markerAttributes.zIndex = zIndex
        //markerAttributes.alpha(0.7f)
        //markerAttributes.anchor = .anchor(0.5f, 1.0f)

        var marker = mapInterface!!.createMarker(markerAttributes)
        if (marker != null) {
            marker.setMarkerTag(createMarkerTag(rendererId, stop) as Object)
            routeStopMarkers.put(stop.list_item_id!!, marker)
            if (showSelected) {
                currentSelectedStopId = stop.list_item_id
                centerMarker(marker)
            }
        }

        //Add marker with address
        zIndex = if (showSelected) 100.0f; else 90.0f
        /*icon = BitmapDescriptorFactory.fromBitmap(bitmapWithAddress)
        markerOptions = MarkerOptions().position(LatLng(stop.display_position?.lat!!, stop.display_position?.lng!!))
            .icon(icon)
            .visible(false)
            .zIndex(zIndex)
            .anchor(0.5f, 0.65f)
            //.alpha(0.7f)*/

        markerAttributes = BeansMarkerAttributes()
        markerAttributes.bitmap = bitmapWithAddress
        markerAttributes.location =
            GeoPoint(stop.display_position?.lat!!, stop.display_position?.lng!!)
        markerAttributes.isVisible = false
        //markerAttributes.zIndex = zIndex
        //markerAttributes.anchor = .anchor(0.5f, 1.0f)


        marker = mapInterface!!.createMarker(markerAttributes)
        if (marker != null) {
            marker.setMarkerTag(createMarkerTag(rendererId, stop) as Object)
            routeStopMarkersWithText.put(stop.list_item_id!!, marker)
            if (showSelected) {
                currentSelectedStopId = stop.list_item_id //Redundant ...we did this
                centerMarker(marker)
            }
        }

    }

    private fun updateRouteStopMarkerIconAsLastStop(
        marker: BeansMarkerInterface, stop: RouteStop, attributes: IconAttributes,
        centerMarker: Boolean = true
    ) {
        iconGenerator?.setBackground(TRANSPARENT_DRAWABLE)

        //bitmap without address...
        var bitmap = MarkerIconHelper().getMarkerIconBitmap(
            parentFragment.context!!,
            iconGenerator!!,
            attributes
        )

        var zIndex = 90.0f
        //val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        //val marker = mapView?.getMap()?.addMarker(markerOptions)
        //marker.zIndex = zIndex

        mapInterface?.updateMarkerIcon(marker, bitmap)
        if (centerMarker) {
            centerMarker(marker)
        }
    }

    private fun updateRouteStopMarkerIcon(
        marker: BeansMarkerInterface, stop: RouteStop,
        attributes: IconAttributes,
        centerMarker: Boolean = true
    ) {

        var addressString = stop.address_components?.street
        if (stop.tracking_id != null) {
            addressString = addressString + " SID: " + stop.tracking_id
        }

        iconGenerator?.setBackground(TRANSPARENT_DRAWABLE)

        //bitmap without address
        var bitmap = MarkerIconHelper().getMarkerIconBitmap(
            parentFragment.context!!,
            iconGenerator!!,
            attributes
        )

        mapInterface?.updateMarkerIcon(marker, bitmap)
        if (attributes.showSelected && centerMarker) {
            centerMarker(marker)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MapMoved) {
        handleMapMovement(event)
    }

    private fun handleMapMovement(event: MapMoved) {
        if (mapInterface!!.isMapReady()) {
            if (showMarkersWithAddressFeature) {
                if (mapInterface!!.getCurrentZoomLevel() >= 14) {
                    if (!showingAddressOnMarkers) {
                        showingAddressOnMarkers = true
                        switchMarkersOnMap()
                    }
                } else {
                    if (showingAddressOnMarkers) {
                        showingAddressOnMarkers = false
                        switchMarkersOnMap()
                    }
                }
            } else {
                if (showingAddressOnMarkers) {
                    showingAddressOnMarkers = false
                    switchMarkersOnMap()
                }
            }
        }
    }

    private fun switchMarkersOnMap() {
        if (showingAddressOnMarkers) {
            for (marker in routeStopMarkers.values) {
                mapInterface?.hideMarker(marker)
            }
            for (marker in routeStopMarkersWithText.values) {
                if (showingCompletedMarkersFeature) {
                    mapInterface?.showMarker(marker)
                } else {
                    var stop = getDataFromTag(marker.getMarkerTag()!!, rendererId) as RouteStop
                    if (stop.status == RouteStopStatus.NEW) {
                        mapInterface?.showMarker(marker)
                    } else {
                        mapInterface?.hideMarker(marker)
                    }

                }
            }
        } else {
            for (marker in routeStopMarkersWithText.values) {
                mapInterface?.hideMarker(marker)
            }

            for (marker in routeStopMarkers.values) {
                if (showingCompletedMarkersFeature) {
                    mapInterface?.showMarker(marker)
                } else {
                    var stop = getDataFromTag(marker.getMarkerTag()!!, rendererId) as RouteStop
                    if (stop.status == RouteStopStatus.NEW) {
                        mapInterface?.showMarker(marker)
                    } else {
                        mapInterface?.hideMarker(marker)
                    }
                }
            }
        }
    }

    override fun onMapMarkerClicked(marker: BeansMarkerInterface) {
        super.onMapMarkerClicked(marker)
        handleMarkerClick(marker)
    }

    private fun handleMarkerClick(marker: BeansMarkerInterface) {
        if (mapInterface!!.isMapReady()) {
            (parentFragment as BeansMapFragmentRouteViewImpl).hideKeyboard()
            var newStop = getDataFromTag(marker.getMarkerTag()!!, rendererId) as RouteStop
            routeDataViewModel?.setCurrentStop(newStop)
            currentStopViewModel?.setCurrentRouteStop(newStop)

            EventBus.getDefault().post(ShowCard())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(statusUpdate: UpdateStopStatus) {
        updateViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: HideCard) {
        if (currentStopViewModel?.currentRouteStop != null) {
            routeDataViewModel?.setCurrentStop(null)
            currentStopViewModel?.setCurrentRouteStop(null)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GoToNextStop) {
        showNextStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GoToPreviousStop) {
        //showPrevStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ResetStopViews) {
        savedLocation = null
        updateViews()
    }

    override fun onPanelStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            var currentStop = routeDataViewModel?.getCurrentStop()
            if (currentStop != null && currentSelectedStopId != null) {
                routeDataViewModel?.setCurrentStop(null)
            }
        }
    }

    override fun unplug() {
        super.unplug()
    }

    override fun clearContent() {
        for (marker in routeStopMarkers.values) {
            mapInterface?.removeMarker(marker)
        }
        for (marker in routeStopMarkersWithText.values) {
            mapInterface?.removeMarker(marker)
        }

        for (polyLine in polylineArray) {
            mapInterface?.removePolyline(polyLine)
        }

        routeStopMarkers.clear()
        routeStopMarkersWithText.clear()
        polylineArray.clear()
    }

    private fun showAlert(str: String) {
        val dialog = AlertDialog.Builder(parentFragment.context)
        dialog.setMessage(str)
        dialog.setTitle("Optimize")
        val alertDialog = dialog.create()
        alertDialog.show()
    }

    fun showPrevStop() {
    }

    fun showNextStop() {
        routeDataViewModel?.moveToNextStop()
    }
}
