package ai.beans.common.maps.mapproviders

import ai.beans.common.R
import ai.beans.common.analytics.AnalyticsEvents
import ai.beans.common.analytics.fireEvent
import ai.beans.common.maps.boundingbox.BeansMapBoundingBox
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.maps.markers.GoogleMapMarker
import ai.beans.common.maps.polylines.BeansPolyline
import ai.beans.common.maps.polylines.GooglePolyline
import ai.beans.common.maps.tiledownlader.BeansTileInfo
import ai.beans.common.pojo.GeoPoint
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*

class BeansMapGoogleImpl : MapView, GoogleMap.OnCameraIdleListener, BeansMapInterface {

    private var mMap: GoogleMap? = null
    var mCurrentLocation = LatLng(0.0, 0.0)
    var zoomLevel = 16.0f
    var mapListener: BeansMapViewListener? = null
    var isMyLocationEnabled: Boolean = false
    var locationSource: LocationSource? = null
    var isMapReady = MutableLiveData<Boolean>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    init {
        setupMap()
    }

    @SuppressLint("MissingPermission")
    fun setupMap(
        mapSkinResourceFile: Int? = null,
        zoomLevel: Float? = null,
        location: LatLng? = null
    ) {
        getMapAsync {
            Log.d("BeansMapView", "Map is ready")
            isMapReady.value = true
            mMap = it

            if (mapSkinResourceFile != null) {
                mMap?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this.context,
                        mapSkinResourceFile
                    )
                )
            } else {
                val mode =
                    context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
                when (mode) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        setupMapSkin(R.raw.google_map_skin_dark_mode)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        setupMapSkin(R.raw.google_map_skin)
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        setupMapSkin(R.raw.google_map_skin)
                    }
                    else -> {
                        setupMapSkin(R.raw.google_map_skin)
                    }
                }
            }
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap?.setOnCameraIdleListener(this)

            mMap?.uiSettings?.isMyLocationButtonEnabled = false
            if (locationSource != null) {
                mMap?.setLocationSource(locationSource)
            }
            enableMyLocation(true)

            mMap?.setOnMapClickListener(GoogleMap.OnMapClickListener {
                mapListener?.onMapClicked(it)
            })
            mMap?.setOnMapLongClickListener(GoogleMap.OnMapLongClickListener {
                mapListener?.onMapLongClicked(it)
            })
            mMap?.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
                mapListener?.onMapMarkerClicked(marker.tag as BeansMarkerInterface)
                true
            })
            mMap?.setOnInfoWindowClickListener {
                mapListener?.onInfoWindowClicked(it.tag as BeansMarkerInterface)
            }
            mMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(marker: Marker?) {
                    mapListener?.onMarkerDragEnd(marker?.tag as BeansMarkerInterface)
                }

                override fun onMarkerDragStart(marker: Marker?) {
                    mapListener?.onMarkerDragStart(marker?.tag as BeansMarkerInterface)
                }

                override fun onMarkerDrag(marker: Marker?) {
                    mapListener?.onMarkerDrag(marker?.tag as BeansMarkerInterface)
                }

            })
            Log.d("Zoom_Level", "mapReady")
            Log.d("Zoom_Level_width", width.toString())
            Log.d("Zoom_Level_height", height.toString())
            mapListener?.mapReady()
        }

    }

    override fun onCameraIdle() {
        Log.d("Zoom_Level", "onCameraIdle")
        Log.d("Zoom_Level_width", width.toString())
        Log.d("Zoom_Level_height", height.toString())

        if (mMap?.cameraPosition?.zoom != null) {
            zoomLevel = mMap?.cameraPosition?.zoom!!
            Log.d("Zoom_Level", zoomLevel.toString())
            overlay
        }
        val oldLatLng = mCurrentLocation
        mCurrentLocation = mMap?.getCameraPosition()?.target!!

        val boundingBox = mMap?.getProjection()?.visibleRegion?.latLngBounds

        //Calculate distance moved (if we have a previous location)
        val loc1 = Location("")
        loc1.latitude = mCurrentLocation.latitude
        loc1.longitude = mCurrentLocation.longitude

        val loc2 = Location("")
        loc2.latitude = oldLatLng.latitude
        loc2.longitude = oldLatLng.longitude
        var distanceInMeters = loc1.distanceTo(loc2).toDouble()

        mapListener?.mapMoved(boundingBox, oldLatLng, mCurrentLocation, distanceInMeters)
    }

    fun setCurrentLocation(location: Location, zoomLevel: Float? = null, animate: Boolean = true) {
        if (mMap != null) {
            Log.d("Zoom_Level", "Setting current Location")

            if (zoomLevel != null) {
                this.zoomLevel = zoomLevel
            }
            Log.d("Zoom_Level = ", this.zoomLevel.toString())
            if (animate) {
                mMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location?.latitude!!,
                            location?.longitude!!
                        ), this.zoomLevel!!
                    )
                )
            } else {
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location?.latitude!!,
                            location?.longitude!!
                        ), this.zoomLevel!!
                    )
                )
            }
        }
    }

    fun setCurrentBounds(
        latlngBounds: LatLngBounds,
        width: Int,
        height: Int,
        padding: Int,
        animate: Boolean = false
    ) {
        Log.d("Zoom_Level", "setCurrentBounds 1")
        Log.d("Zoom_Level_width", width.toString())
        Log.d("Zoom_Level_height", height.toString())

        if (mMap != null && mCurrentLocation != null) {
            Log.d("Zoom_Level", "setCurrentBounds 2")
            Log.d("Zoom_Level_width", width.toString())
            Log.d("Zoom_Level_height", height.toString())
            if (animate) {
                Log.d("Zoom_Level", "ANIMATE")
                mMap?.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        latlngBounds,
                        width,
                        height,
                        padding
                    )
                )
            } else {
                Log.d("Zoom_Level", "NO ANIMATE")
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        latlngBounds,
                        width,
                        height,
                        padding
                    )
                )
            }
        }
    }

    fun setupMapSkin(mapSkinResourceFile: Int? = null) {
        if (mapSkinResourceFile != null) {
            mMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.context,
                    mapSkinResourceFile
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation(flag: Boolean = true) {
        isMyLocationEnabled = flag
        try {
            Log.d("BeansMapView", "Enabling Location")
            mMap?.isMyLocationEnabled = isMyLocationEnabled
            //Show the "My Location button
        } catch (ex: SecurityException) {
            Log.d("BeansMapView", "Exception" + ex.toString())
        }
    }

    fun setMapMovementListener(listener: BeansMapViewListener) {
        mapListener = listener
        if (isMapReady.value != null && isMapReady.value!!) {
            mapListener?.mapReady()
        }
        //Now that we have a listener attached....lets enable the map
//        getMapAsync {
//            isMapReady = true
//            mMap = it
//            listener?.mapReady()
//        }
    }

    override fun isSatelliteViewEnabled(): Boolean {
        return mMap?.mapType == GoogleMap.MAP_TYPE_SATELLITE
    }

    override fun enableSatelliteView(shouldEnableSatellite: Boolean) {
        if (shouldEnableSatellite && mMap?.mapType == GoogleMap.MAP_TYPE_SATELLITE) {
            return
        }
        if (!shouldEnableSatellite && mMap?.mapType != GoogleMap.MAP_TYPE_SATELLITE) {
            return
        }
        if (shouldEnableSatellite) {
            fireEvent(AnalyticsEvents.SELECTED_SATELLITE_VIEW, null)
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        } else {
            fireEvent(AnalyticsEvents.SELECTED_MAP_VIEW, null)
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    fun clearMap() {
        mMap?.clear()
    }

    fun getMap(): GoogleMap? {
        return mMap
    }

    override fun setLocationProvider(locationSource: LocationSource) {
        this.locationSource = locationSource
        Log.d("BeansMapView", "Setting Location Provider")
        mMap?.setLocationSource(locationSource)
        enableMyLocation(true)
    }

    fun renderPolygon(polygonOptions: PolygonOptions): Polygon? {
        return mMap?.addPolygon(polygonOptions)
    }

    fun zoomIn() {
        mMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    mCurrentLocation.latitude!!,
                    mCurrentLocation.longitude!!
                ), zoomLevel + 1
            )
        )
    }

    override fun registerMapEventListener(listener: BeansMapViewListener) {
        setMapMovementListener(listener)
    }

    override fun createMarker(attributes: BeansMarkerAttributes): BeansMarkerInterface {
        val bmpDescriptor = BitmapDescriptorFactory.fromBitmap(attributes.bitmap)

        val markerOptions = MarkerOptions().position(
            LatLng(
                attributes.location!!.lat!!,
                attributes.location!!.lng!!
            )
        ).icon(bmpDescriptor).visible(attributes.isVisible).draggable(attributes.isDraggable)

        //var zIndex = if (attributes.showOnTop) 100.0f; else 90.0f
        markerOptions.zIndex(90.0f)

        var marker = getMap()?.addMarker(markerOptions)
        return GoogleMapMarker(marker!!)
    }

    override fun hideMarker(markerInterface: BeansMarkerInterface) {
        if (markerInterface is GoogleMapMarker) {
            var marker = markerInterface as GoogleMapMarker
            marker.marker.isVisible = false
        }
    }

    override fun updateMarkerIcon(markerInterface: BeansMarkerInterface, icon: Bitmap) {
        if (markerInterface is GoogleMapMarker) {
            var marker = markerInterface as GoogleMapMarker
            val bmpDescriptor = BitmapDescriptorFactory.fromBitmap(icon)
            marker.marker.setIcon(bmpDescriptor)
        }
    }


    override fun removeMarker(markerInterface: BeansMarkerInterface) {
        if (markerInterface is GoogleMapMarker) {
            var marker = markerInterface as GoogleMapMarker
            marker.marker.remove()
        }
    }

    override fun showMarker(markerInterface: BeansMarkerInterface) {
        if (markerInterface is GoogleMapMarker) {
            var marker = markerInterface as GoogleMapMarker
            marker.marker.isVisible = true
        }
    }

    override fun createPolyline(points: ArrayList<GeoPoint>): BeansPolyline? {
        var polylineOptions = PolylineOptions()
        for (point in points) {
            polylineOptions.add(LatLng(point.lat!!, point.lng!!))
        }
        polylineOptions.width(7f)
        polylineOptions.color(resources.getColor(R.color.route_stop_path))

        var polyline = mMap?.addPolyline(polylineOptions)
        if (polyline != null) {
            return GooglePolyline(polyline)
        } else {
            return null
        }
    }

    override fun showPolyline(polyline: BeansPolyline) {
        if (polyline is GooglePolyline) {
            var googlePolyline = polyline as GooglePolyline
            googlePolyline.polyline?.isVisible = true
        }
    }

    override fun hidePolyline(polyline: BeansPolyline) {
        if (polyline is GooglePolyline) {
            var googlePolyline = polyline as GooglePolyline
            googlePolyline.polyline?.isVisible = false
        }
    }

    override fun removePolyline(polyline: BeansPolyline) {
        if (polyline is GooglePolyline) {
            var googlePolyline = polyline as GooglePolyline
            googlePolyline.polyline?.remove()
        }
    }


    override fun getCurrentLocation(): GeoPoint? {
        if (mCurrentLocation != null) {
            var loc = GeoPoint()
            loc.lat = mCurrentLocation.latitude
            loc.lng = mCurrentLocation.longitude
            return loc
        } else {
            return null
        }
    }

    override fun setCurrentLocation(
        loc: GeoPoint,
        zoomLevel: Float?,
        animate: Boolean
    ) {
        var location = Location("")
        location.latitude = loc.lat!!
        location.longitude = loc.lng!!

        setCurrentLocation(location, zoomLevel, animate)
    }

    override fun setCurrentBounds(
        points: ArrayList<GeoPoint>,
        width: Int,
        height: Int,
        padding: Int,
        animate: Boolean
    ) {
        var totalBounds: LatLngBounds? = null
        for (geoPoint in points) {
            if (totalBounds == null) {
                totalBounds = LatLngBounds(
                    LatLng(geoPoint.lat!!, geoPoint.lng!!),
                    LatLng(geoPoint.lat!!, geoPoint.lng!!)
                )
            } else {
                totalBounds = totalBounds!!.including(LatLng(geoPoint.lat!!, geoPoint.lng!!))
            }
        }

        //We got the bounds
        totalBounds?.let {
            this@BeansMapGoogleImpl?.setCurrentBounds(it, width, height, padding, animate)
            //generateLinkDisplayAssets(routes)
        }
    }

    override fun getVisibleMapBounds(): BeansMapBoundingBox {
        mMap?.projection!!.visibleRegion.farLeft.latitude
        return BeansMapBoundingBox(
            mMap?.projection!!.visibleRegion.farLeft.latitude,
            mMap?.projection!!.visibleRegion.farLeft.longitude,
            mMap?.projection!!.visibleRegion.farRight.latitude,
            mMap?.projection!!.visibleRegion.farRight.longitude
        )
    }

    override fun getCurrentZoomLevel(): Float {
        return zoomLevel
    }

    override fun setCurrentZoomLevel(zoom: Float) {
        zoomLevel = zoom
    }


    override fun onLifecycleOwnerCreate(savedBundle: Bundle?) {
        onCreate(savedBundle)
    }

    override fun onLifecycleOwnerStart() {
        onStart()
    }

    override fun onLifecycleOwnerResume() {
        onResume()
    }

    override fun onLifecycleOwnerPause() {
        onPause()
    }

    override fun onLifecycleOwnerStop() {
        onStop()
    }

    override fun onLifecycleOwnerDestroy() {
        onDestroy()
    }

    override fun onLowMemoryWarning() {
        onLowMemory()
    }

    override fun getTileInfoForCurrentBounds(minZoom: Int, maxZoom: Int): BeansTileInfo? {
        //Tile downloading not available for google maps. Google does not allow this (yet!)
        return null
    }

    override fun downloadTilesForVisibleRegion(context: Context, minZoom: Int, maxZoom: Int) {
        //No such thing for Google maps
    }

    override fun clearMapContents() {
        clearMap()
    }

    override fun fromScreenLocation(point: Point): GeoPoint {
        var loc = mMap?.projection!!.fromScreenLocation(point)
        return GeoPoint(loc.latitude, loc.longitude)
    }

    override fun getBoundsForLocations(points: ArrayList<GeoPoint>): BeansMapBoundingBox {
        var totalBounds: LatLngBounds? = null
        for (geoPoint in points) {
            if (totalBounds == null) {
                totalBounds = LatLngBounds(
                    LatLng(geoPoint.lat!!, geoPoint.lng!!),
                    LatLng(geoPoint.lat!!, geoPoint.lng!!)
                )
            } else {
                totalBounds = totalBounds!!.including(LatLng(geoPoint.lat!!, geoPoint.lng!!))
            }
        }

        var top = totalBounds!!.northeast.latitude
        var right = totalBounds!!.northeast.longitude

        var bottom = totalBounds!!.southwest.latitude
        var left = totalBounds!!.southwest.longitude
        return BeansMapBoundingBox(left, top, right, bottom)
    }

    override fun getMapReadyFlag(): MutableLiveData<Boolean> {
        return isMapReady
    }

}