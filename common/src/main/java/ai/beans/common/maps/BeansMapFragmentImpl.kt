package ai.beans.common.maps

import ai.beans.common.*
import ai.beans.common.R
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.events.*
import ai.beans.common.location.LocationHolder
import ai.beans.common.maps.*
import ai.beans.common.maps.boundingbox.BeansMapBoundingBox
import ai.beans.common.maps.mapproviders.BeansMapInterface
import ai.beans.common.maps.mapproviders.BeansMapViewListener
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.maps.polylines.BeansPolyline
import ai.beans.common.maps.tiledownlader.BeansTileInfo
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.utils.MultiStateObserver
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class BeansMapFragmentImpl : BeansFragment(), BeansMapViewListener, BeansMapFragmentInterface {

    private var LOCATION_SERVICE_OK = 1
    private var MAP_VIEW_OK = 2

    private var mapviewInterface: BeansMapInterface? = null
    private var locationHolder: LocationHolder? = null
    private var myLocationButton: RoundMapButton? = null
    private var satelliteButton: RoundMapButton? = null
    private var offlineButton: RoundMapButton? = null
    private var backButton: RoundMapButton?  = null

    private var isLocationServiceRunning = false

    var savedZoom: Float? = null
    var savedLocation: GeoPoint? = null

    var multiStateObserver: MultiStateObserver? = null

    var mapViewListenerMap = HashMap<String, BeansMapViewListener>()

    var useOnlineMaps = true
    var onlineMapWidgetContainer: FrameLayout? = null
    var offlineMapWidgetContainer: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            useOnlineMaps = savedInstanceState.getBoolean("use_google_maps", false)
        } else {
            if (arguments != null) {
                useOnlineMaps = arguments!!.getBoolean("use_google_maps", false)!!
            } else {
                useOnlineMaps = true
            }
        }

        locationHolder = ViewModelProviders.of(
            activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)
        ).get(
            LocationHolder::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_map_view, container, false)

        if (savedInstanceState != null) {
            savedZoom = savedInstanceState.getFloat("MAP_ZOOM_LEVEL")
            var lat = savedInstanceState?.getDouble("MAP_LOCATION_LATITUDE")
            var long = savedInstanceState?.getDouble("MAP_LOCATION_LONGITUDE")
            if (lat != null && long != null) {
                savedLocation = GeoPoint()
                savedLocation!!.lat = lat
                savedLocation!!.lng = long
            }
        }

        onlineMapWidgetContainer = v.findViewById(R.id.mapWidgetContainer)
        offlineMapWidgetContainer = v.findViewById(R.id.offlineMapWidgetContainer)
        if (useOnlineMaps) {
            onlineMapWidgetContainer?.visibility = View.VISIBLE
            offlineMapWidgetContainer?.visibility = View.GONE

            mapviewInterface = v.findViewById<View>(R.id.embedded_map_view) as BeansMapInterface
        } else {
            onlineMapWidgetContainer?.visibility = View.GONE
            offlineMapWidgetContainer?.visibility = View.VISIBLE

            mapviewInterface =
                v.findViewById<View>(R.id.embedded_map_view_offline) as BeansMapInterface
        }

        mapviewInterface?.let {
            it.onLifecycleOwnerCreate(savedInstanceState)
            it.registerMapEventListener(this@BeansMapFragmentImpl)
        }

        myLocationButton = v.findViewById(R.id.location_button)
        myLocationButton?.mapButtonlistener = object : RoundMapButton.MapButtonListener {
            override fun buttonClicked(btnId: Int, state: Boolean) {
                if (isLocationServiceRunning) {
                    var loc = GeoPoint()
                    loc.lat = locationHolder!!.currentLocation!!.latitude
                    loc.lng = locationHolder!!.currentLocation!!.longitude
                    mapviewInterface?.setCurrentLocation(loc, null, true)
                }
            }
        }

        satelliteButton = v.findViewById(R.id.satellite_button)
        satelliteButton?.mapButtonlistener = object : RoundMapButton.MapButtonListener {
            override fun buttonClicked(btnId: Int, state: Boolean) {
                if (isLocationServiceRunning) {
                    satelliteButton?.toggleButton()
                    if (isSatelliteViewEnabled()) {
                        enableSatelliteView(false)
                    } else {
                        enableSatelliteView(true)
                    }
                }
            }
        }

        offlineButton = v.findViewById(R.id.offline_button)
        offlineButton?.mapButtonlistener = object : RoundMapButton.MapButtonListener {
            override fun buttonClicked(btnId: Int, state: Boolean) {
                if (isLocationServiceRunning) {
                    offlineButton?.toggleButton()
                    useOnlineMaps = !useOnlineMaps
                    if (useOnlineMaps) {
                        onlineMapWidgetContainer?.visibility = View.VISIBLE
                        offlineMapWidgetContainer?.visibility = View.GONE

                        mapviewInterface =
                            v.findViewById<View>(R.id.embedded_map_view) as BeansMapInterface
                        MainScope().launch {
                            EventBus.getDefault().post(ResetStopViews())
                        }
                    } else {
                        onlineMapWidgetContainer?.visibility = View.GONE
                        offlineMapWidgetContainer?.visibility = View.VISIBLE

                        mapviewInterface =
                            v.findViewById<View>(R.id.embedded_map_view_offline) as BeansMapInterface
                        MainScope().launch {
                            EventBus.getDefault().post(ResetStopViews())
                        }
                    }
                }
            }
        }

        backButton = v.findViewById(R.id.nav_back)
        backButton?.mapButtonlistener = object : RoundMapButton.MapButtonListener {
            override fun buttonClicked(btnId: Int, state: Boolean) {
                activity?.onBackPressed()
            }
        }

        multiStateObserver = MultiStateObserver()
        multiStateObserver?.setStateIds(arrayListOf<Int>(LOCATION_SERVICE_OK, MAP_VIEW_OK))

        multiStateObserver?.setObserverFor(
            viewLifecycleOwner,
            locationHolder?.isLocationAvailable!!,
            LOCATION_SERVICE_OK
        )
        multiStateObserver?.setObserverFor(
            viewLifecycleOwner,
            mapviewInterface!!.getMapReadyFlag(),
            MAP_VIEW_OK
        )
        multiStateObserver?.multiStateIsReady?.observe(viewLifecycleOwner, Observer {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                //Both location service and map are ready
                isLocationServiceRunning = true
                mapviewInterface?.setLocationProvider(locationHolder!!)
                Log.d("Zoom_Level", "mapReady  and location service connected")
            }
        })

        return v
    }

    override fun onResume() {
        super.onResume()
        getMainActivity()?.getWindow()
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (mapviewInterface != null) {
            mapviewInterface!!.onLifecycleOwnerResume()
        }

        view?.let {
            setupMap()
        }
    }

    override fun onPause() {
        mapviewInterface?.let {
            it.onLifecycleOwnerPause()
        }
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapviewInterface?.let {
            it.onLifecycleOwnerStop()
        }
    }

    override fun onDestroy() {
        if (mapviewInterface != null) {
            try {
                mapviewInterface.let { it?.onLifecycleOwnerDestroy() }
            } catch (e: NullPointerException) {
                Log.e("Error", "Error while attempting MapView.onDestroy(), ignoring exception", e)
            }
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isMapReady()) {
            outState.putFloat("MAP_ZOOM_LEVEL", mapviewInterface!!.getCurrentZoomLevel())
            var location = mapviewInterface?.getCurrentLocation()
            outState.putDouble("MAP_LOCATION_LATITUDE", location!!.lat!!)
            outState.putDouble("MAP_LOCATION_LONGITUDE", location!!.lng!!)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapviewInterface?.let { it.onLowMemoryWarning() }
    }

    private fun hidePermissionOverlay() {
        var overlay = view?.findViewById<RelativeLayout>(R.id.overlay)
        overlay?.visibility = View.GONE
    }

    private fun showPermissionOverlay() {

        var overlay = view?.findViewById<RelativeLayout>(R.id.overlay)
        var retryButton = view?.findViewById<Button>(R.id.retryButton)
        var settingMessage = view?.findViewById<TextView>(R.id.permissionInfo_settings_text)
        var permissionInfoMessage = view?.findViewById<TextView>(R.id.permissionInfo)

        if (permmisionManager.isPermissionEverRequested(Manifest.permission.ACCESS_FINE_LOCATION)) {

            overlay?.visibility = View.VISIBLE

            permissionInfoMessage?.visibility = View.VISIBLE

            if (permmisionManager.isPermissionDeniedForever(Manifest.permission.ACCESS_FINE_LOCATION)) {
                settingMessage?.visibility = View.VISIBLE
                retryButton?.visibility = View.GONE
            } else {
                settingMessage?.visibility = View.GONE
                retryButton?.visibility = View.VISIBLE

            }
            retryButton?.setOnClickListener {
                permmisionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            permmisionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun createMarker(attributes: BeansMarkerAttributes): BeansMarkerInterface {
        return mapviewInterface?.createMarker(attributes)!!
    }

    override fun createPolyline(points: ArrayList<GeoPoint>): BeansPolyline? {
        return mapviewInterface!!.createPolyline(points)
    }

    override fun downloadTilesForVisibleRegion(context: Context, minZoom: Int, maxZoom: Int) {
        mapviewInterface!!.downloadTilesForVisibleRegion(context, minZoom, maxZoom)
    }

    override fun fromScreenLocation(point: Point): GeoPoint {
        return mapviewInterface!!.fromScreenLocation(point)
    }

    override fun getBoundsForLocations(points: ArrayList<GeoPoint>): BeansMapBoundingBox {
        return mapviewInterface!!.getBoundsForLocations(points)
    }

    override fun getCurrentZoomLevel(): Float {
        return mapviewInterface!!.getCurrentZoomLevel()
    }

    override fun getMapReadyFlag(): MutableLiveData<Boolean> {
        return mapviewInterface!!.getMapReadyFlag()
    }

    override fun getTileInfoForCurrentBounds(minZoom: Int, maxZoom: Int): BeansTileInfo? {
        return mapviewInterface!!.getTileInfoForCurrentBounds(minZoom, maxZoom)
    }

    override fun getVisibleMapBounds(): BeansMapBoundingBox {
        return mapviewInterface!!.getVisibleMapBounds()
    }

    override fun hideMarker(marker: BeansMarkerInterface) {
        mapviewInterface?.hideMarker(marker)
    }

    override fun hidePolyline(polyline: BeansPolyline) {
        mapviewInterface?.hidePolyline(polyline)
    }

    override fun isMapReady(): Boolean {
        var flag = mapviewInterface!!.getMapReadyFlag()
        if (flag.value == null) {
            return false
        } else {
            return flag.value!!
        }
    }

    override fun registerMapEventListener(rendererId: String, listener: BeansMapViewListener) {
        mapViewListenerMap.put(rendererId, listener)
    }

    override fun removeMarker(marker: BeansMarkerInterface) {
        mapviewInterface?.removeMarker(marker)
    }

    override fun removePolyline(polyline: BeansPolyline) {
        mapviewInterface?.removePolyline(polyline)
    }

    override fun updateMarkerIcon(marker: BeansMarkerInterface, icon: Bitmap) {
        mapviewInterface?.updateMarkerIcon(marker, icon)
    }

    override fun setCurrentBounds(
        points: ArrayList<GeoPoint>,
        width: Int,
        height: Int,
        padding: Int,
        animate: Boolean
    ) {
        mapviewInterface?.setCurrentBounds(points, width, height, padding, animate)
    }

    override fun setCurrentLocation(location: Location, zoomLevel: Float?, animate: Boolean) {
        var loc = GeoPoint()
        loc.lat = location.latitude
        loc.lng = location.longitude
        mapviewInterface?.setCurrentLocation(loc, zoomLevel, animate)
    }

    override fun showMarker(marker: BeansMarkerInterface) {
        mapviewInterface?.showMarker(marker)
    }

    override fun showPolyline(polyline: BeansPolyline) {
        TODO("Not yet implemented")
    }

    override fun isSatelliteViewEnabled(): Boolean {
        return mapviewInterface != null && mapviewInterface!!.isSatelliteViewEnabled()
    }

    override fun enableSatelliteView(shouldEnableSatellite: Boolean) {
        if (mapviewInterface == null) {
            return
        }
        if (shouldEnableSatellite && mapviewInterface!!.isSatelliteViewEnabled()) {
            return
        }
        if (!shouldEnableSatellite && !mapviewInterface!!.isSatelliteViewEnabled()) {
            return
        }
        if (shouldEnableSatellite) {
            mapviewInterface!!.enableSatelliteView(true)
        } else {
            mapviewInterface!!.enableSatelliteView(false)
        }
    }

    private fun checkForLocationPermission(v: View) {
        var overlay = v.findViewById<RelativeLayout>(R.id.overlay)
        var retryButton = v.findViewById<Button>(R.id.retryButton)
        var settingMessage = v.findViewById<TextView>(R.id.permissionInfo_settings_text)
        var permissionInfoMessage = v.findViewById<TextView>(R.id.permissionInfo)

        if (permmisionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            overlay.visibility = View.GONE

        } else {
            if (permmisionManager.isPermissionEverRequested(Manifest.permission.ACCESS_FINE_LOCATION)) {

                overlay.visibility = View.VISIBLE

                permissionInfoMessage.visibility = View.VISIBLE

                if (permmisionManager.isPermissionDeniedForever(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    settingMessage.visibility = View.VISIBLE
                    retryButton.visibility = View.GONE
                } else {
                    settingMessage.visibility = View.GONE
                    retryButton.visibility = View.VISIBLE

                }
                retryButton.setOnClickListener {
                    permmisionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                permmisionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun setScreenName() {
        screenName = "map"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permmisionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            //setupMap()
            //We have permission for location....
            //Lets setup the map and the location service
            MainScope().launch {
                locationHolder?.setupService()
            }
        }
    }

    override fun mapMoved(
        bounds: LatLngBounds?,
        oldCenter: LatLng?,
        newCenter: LatLng?,
        distanceMoved: Double?
    ) {
        savedLocation = mapviewInterface?.getCurrentLocation()!!
        savedZoom = mapviewInterface?.getCurrentZoomLevel()

        if (distanceMoved != null && distanceMoved > 0) {
            //Tell parent...
            parentBus?.post(MapMoved(bounds, oldCenter, newCenter, distanceMoved))
            //and all registered listeners
            for (listener in mapViewListenerMap.values) {
                listener.mapMoved(bounds, oldCenter, newCenter, distanceMoved)
            }
        }
    }

    override fun mapReady() {
        //Map is now ready and attached...
        //Check if we have location service ready...
        Log.d("Zoom_Level", "mapReady")
        if (savedZoom != null) {
            mapviewInterface?.setCurrentZoomLevel(savedZoom!!)
        } else {
            savedZoom = 16.0f
        }
        if (savedLocation != null) {
            mapviewInterface?.setCurrentLocation(savedLocation!!, savedZoom, false)
        }
    }

    override fun onMapClicked(location: LatLng) {
        //Tell parent...
        parentBus?.post(MapClicked(location))
        //and all registered listeners
        for (listener in mapViewListenerMap.values) {
            listener.onMapClicked(location)
        }
    }

    override fun onMapLongClicked(location: LatLng) {
        //Tell parent...
        parentBus?.post(MapLongClicked(location))
        //and all registered listeners
        for (listener in mapViewListenerMap.values) {
            listener.onMapLongClicked(location)
        }
    }

    override fun onMapMarkerClicked(marker: BeansMarkerInterface) {
        //Tell parent...
        parentBus?.post(MarkerClicked(marker))
        var tag = marker.getMarkerTag()
        if (tag != null && tag is MarkerDataContainer<*>) {
            var markerContainer = tag as MarkerDataContainer<*>
            var listener = mapViewListenerMap.get(markerContainer.id)
            listener?.onMapMarkerClicked(marker)
        }
    }

    override fun onMarkerDrag(marker: BeansMarkerInterface?) {
        if (marker != null) {
            //Tell parent...
            parentBus?.post(MarkerDragged(marker))
            //and all registered listeners
            var tag = marker.getMarkerTag()
            if (tag != null && tag is MarkerDataContainer<*>) {
                var markerContainer = tag as MarkerDataContainer<*>
                var listener = mapViewListenerMap.get(markerContainer.id)
                listener?.onMarkerDrag(marker)
            }
        }

    }

    override fun onInfoWindowClicked(marker: BeansMarkerInterface) {
        //Tell parent...
        parentBus?.post(MarkerInfoWindowClicked(marker))
        var tag = marker.getMarkerTag()
        if (tag != null && tag is MarkerDataContainer<*>) {
            var markerContainer = tag as MarkerDataContainer<*>
            var listener = mapViewListenerMap.get(markerContainer.id)
            listener?.onInfoWindowClicked(marker)
        }

    }

    override fun onMarkerDragEnd(marker: BeansMarkerInterface?) {
        if (marker != null) {
            //Tell parent...
            parentBus?.post(MarkerDragEnd(marker))
            var tag = marker.getMarkerTag()
            if (tag != null && tag is MarkerDataContainer<*>) {
                var markerContainer = tag as MarkerDataContainer<*>
                var listener = mapViewListenerMap.get(markerContainer.id)
                listener?.onMarkerDragEnd(marker)
            }
        }
    }

    override fun onMarkerDragStart(marker: BeansMarkerInterface?) {
        if (marker != null) {
            //Tell parent...
            parentBus?.post(MarkerDragStart(marker))
            var tag = marker.getMarkerTag()
            //and all registered listeners
            if (tag != null && tag is MarkerDataContainer<*>) {
                var markerContainer = tag as MarkerDataContainer<*>
                var listener = mapViewListenerMap.get(markerContainer.id)
                listener?.onMarkerDragStart(marker)
            }
        }
    }

    fun setupMap() {
        MainScope().launch {
            if (!permmisionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionOverlay()
            } else {
                hidePermissionOverlay()
                locationHolder?.setupService()
            }
        }
    }

    private fun clearMapAssets() {
        //Remove
        //1. Polygons
        //2. PolyLines
        //3. Markers
        mapviewInterface?.clearMapContents()
    }

    fun getVisibleRect(): Rect {
        var visibleRect = Rect()
        view?.getGlobalVisibleRect(visibleRect)
        return visibleRect
    }
}