package ai.beans.common.maps.mapproviders

import ai.beans.common.R
import ai.beans.common.maps.boundingbox.BeansMapBoundingBox
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.maps.markers.OSMMapMarker
import ai.beans.common.maps.polylines.BeansPolyline
import ai.beans.common.maps.polylines.OSMPolyline
import ai.beans.common.maps.tiledownlader.BeansTileInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.osmdroid.events.*
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.MapView.OnFirstLayoutListener
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.cachemanager.CacheManager.CacheManagerCallback
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class BeansMapOSMImpl : MapView, OnFirstLayoutListener, MapListener, MapEventsReceiver,
    BeansMapInterface {
    var mapListener: BeansMapViewListener? = null
    var mCurrentLocation = LatLng(0.0, 0.0)
    var isMapReady = MutableLiveData<Boolean>()
    var POLYLINE_OVERLAY_INDEX = 1
    var MAP_TOUCH_OVERLAY_INDEX = 0
    var tileManager: CacheManager? = null

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, null, null, attrs)


    constructor(context: Context?) : super(context, null, null, null)


    constructor(
        context: Context?,
        aTileProvider: MapTileProviderBase?
    ) : super(context, aTileProvider, null)

    constructor(
        context: Context?,
        aTileProvider: MapTileProviderBase?,
        tileRequestCompleteHandler: Handler?
    ) : super(
        context, aTileProvider, tileRequestCompleteHandler,
        null
    )

    init {
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(context))
        val MAPNIK_BEANS: OnlineTileSourceBase = XYTileSource(
            "Mapnik",
            0, 19, 256, ".png", arrayOf(
                "https://tileserver.beans.ai/tile/"
            ), "© OpenStreetMap contributors",
            TileSourcePolicy(
                2,
                TileSourcePolicy.FLAG_NO_PREVENTIVE
                        or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                        or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
            )
        )

        //setDestroyMode(false)
        setTileSource(MAPNIK_BEANS)
        controller.setZoom(13)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        addOnFirstLayoutListener(this)
        var delayedMapListener = DelayedMapListener(this)
        addMapListener(delayedMapListener)
        var mapOverlay = MapEventsOverlay(this)
        //The map events overlay is the first one in the stack. That way we get map touch events
        //if nobody above handles them
        overlays.add(MAP_TOUCH_OVERLAY_INDEX, mapOverlay)

        //Add an overlay for user location
        val overlay = MyLocationNewOverlay(this)
        //overlay.enableFollowLocation()
        overlay.enableMyLocation()
        overlays.add(overlay)

        //tile cache manager
        tileManager = CacheManager(this)
    }

    override fun onFirstLayout(v: View?, left: Int, top: Int, right: Int, bottom: Int) {
        Log.d("OSMMap", "onFirstLayout")
        isMapReady.value = true
        mapListener?.mapReady()
        setOnClickListener(mapClickListener)

    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        val oldLatLng = mCurrentLocation
        var point = mapCenter

        //Calculate distance moved (if we have a previous location)
        val loc1 = Location("")
        loc1.latitude = mapCenter.latitude
        loc1.longitude = mapCenter.longitude

        val loc2 = Location("")
        loc2.latitude = oldLatLng.latitude
        loc2.longitude = oldLatLng.longitude
        var distanceInMeters = loc1.distanceTo(loc2).toDouble()

        mCurrentLocation = LatLng(mapCenter.latitude, mapCenter.longitude)

        mapListener?.mapMoved(
            LatLngBounds(
                LatLng(boundingBox.latNorth, boundingBox.lonEast),
                LatLng(boundingBox.latNorth, boundingBox.lonEast)
            ),
            LatLng(oldLatLng.latitude, oldLatLng.longitude),
            mCurrentLocation,
            distanceInMeters
        )

        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return true
    }

    var markerClickListener = object : org.osmdroid.views.overlay.Marker.OnMarkerClickListener {
        override fun onMarkerClick(
            marker: org.osmdroid.views.overlay.Marker?,
            mapView: MapView?
        ): Boolean {
            Log.d("Marker", "Clicked")
            if (mapListener == null) {
                Log.d("Marker", "Listenr is null")
            }
            mapListener?.onMapMarkerClicked(marker as BeansMarkerInterface)
            return true
        }

    }

    var markerDragListener = object : org.osmdroid.views.overlay.Marker.OnMarkerDragListener {
        override fun onMarkerDragEnd(marker: org.osmdroid.views.overlay.Marker?) {
        }

        override fun onMarkerDragStart(marker: org.osmdroid.views.overlay.Marker?) {
        }

        override fun onMarkerDrag(marker: org.osmdroid.views.overlay.Marker?) {
        }
    }

    var mapClickListener = OnClickListener { Log.d("Map", "Clicked") }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }

    override fun singleTapConfirmedHelper(tapLoc: GeoPoint?): Boolean {
        var loc = LatLng(tapLoc!!.latitude, tapLoc!!.longitude)
        mapListener?.onMapClicked(loc)
        return true
    }

    override fun registerMapEventListener(listener: BeansMapViewListener) {
        Log.d("Marker", "Setting Listener")
        mapListener = listener
    }

    override fun createMarker(attribs: BeansMarkerAttributes): BeansMarkerInterface {
        val location = GeoPoint(attribs.location!!.lat!!, attribs.location!!.lng!!)
        val marker =
            OSMMapMarker(this)
        marker.setPosition(location)
        marker.setAnchor(
            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
        )
        if (attribs.bitmap != null) {
            marker.icon = BitmapDrawable(resources, attribs.bitmap)
            Log.d("Marker Width", marker.icon!!.intrinsicWidth.toString())
            Log.d("Marker Height", marker.icon!!.intrinsicHeight.toString())
        }
        marker.isDraggable = attribs.isDraggable

        marker.setOnMarkerClickListener(markerClickListener)
        marker.setOnMarkerDragListener(markerDragListener)
        overlays.add(marker)
        return marker
    }

    override fun removeMarker(markerInterface: BeansMarkerInterface) {
        if (markerInterface is Marker) {
            var marker = markerInterface as Marker
            marker.remove(this)
            invalidate()
        }
    }

    override fun showMarker(markerInterface: BeansMarkerInterface) {
        if (markerInterface is Marker) {
            var marker = markerInterface as Marker
            marker.setVisible(true)
            invalidate()
        }
    }

    override fun hideMarker(markerInterface: BeansMarkerInterface) {
        if (markerInterface is Marker) {
            var marker = markerInterface as Marker
            marker.setVisible(false)
            invalidate()
        }
    }

    override fun updateMarkerIcon(markerInterface: BeansMarkerInterface, icon: Bitmap) {
        if (markerInterface is Marker) {
            var marker = markerInterface as Marker
            marker.icon = BitmapDrawable(resources, icon)
            marker.setAnchor(
                org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
            )
            invalidate()
        }
    }

    override fun getCurrentLocation(): ai.beans.common.pojo.GeoPoint? {
        var loc = ai.beans.common.pojo.GeoPoint()
        loc.lat = mCurrentLocation.latitude
        loc.lng = mCurrentLocation.longitude
        return loc
    }

    override fun setCurrentLocation(
        loc: ai.beans.common.pojo.GeoPoint,
        zoomLevel: Float?,
        animate: Boolean
    ) {
        val center = GeoPoint(loc.lat!!, loc.lng!!)
        controller.setCenter(center)

        if (zoomLevel != null) {
            controller.zoomTo(zoomLevel.toDouble())
        }

        if (animate) {
            controller.animateTo(center)
        }
        mCurrentLocation = LatLng(loc.lat!!, loc.lng!!)
    }

    override fun setCurrentBounds(
        points: ArrayList<ai.beans.common.pojo.GeoPoint>,
        width: Int,
        height: Int,
        padding: Int,
        animate: Boolean
    ) {
        var boundingBox: BoundingBox? = null
        var OSMPointsList = ArrayList<GeoPoint>()
        for (point in points) {
            OSMPointsList.add(GeoPoint(point.lat!!, point.lng!!))
        }
        if (OSMPointsList.isNotEmpty()) {
            boundingBox = BoundingBox.fromGeoPoints(OSMPointsList)

            zoomToBoundingBox(boundingBox, animate, padding)
        }
    }

    override fun getVisibleMapBounds(): BeansMapBoundingBox {
        var top = boundingBox.latNorth
        var right = boundingBox.lonEast

        var bottom = boundingBox.latSouth
        var left = boundingBox.lonWest
        return BeansMapBoundingBox(left, top, right, bottom)
    }

    override fun getBoundsForLocations(points: ArrayList<ai.beans.common.pojo.GeoPoint>): BeansMapBoundingBox {
        var boundingBox: BoundingBox? = null
        var OSMPointsList = ArrayList<GeoPoint>()
        for (point in points) {
            OSMPointsList.add(GeoPoint(point.lat!!, point.lng!!))
        }
        boundingBox = BoundingBox.fromGeoPoints(OSMPointsList)
        var top = boundingBox.latNorth
        var right = boundingBox.lonEast

        var bottom = boundingBox.latSouth
        var left = boundingBox.lonWest
        return BeansMapBoundingBox(left, top, right, bottom)
    }

    override fun getMapReadyFlag(): MutableLiveData<Boolean> {
        return isMapReady
    }

    override fun createPolyline(points: ArrayList<ai.beans.common.pojo.GeoPoint>): BeansPolyline? {
        var polyline = Polyline(this)
        polyline.outlinePaint.strokeWidth = 7f
        polyline.outlinePaint.color = resources.getColor(R.color.routeStopPath)

        for (point in points) {
            polyline.addPoint(GeoPoint(point.lat!!, point.lng!!))
        }
        overlays.add(POLYLINE_OVERLAY_INDEX, polyline)
        return OSMPolyline(polyline)
    }

    override fun showPolyline(polyline: BeansPolyline) {
        if (polyline is OSMPolyline) {
            var osmPolyline = polyline as OSMPolyline
            osmPolyline.polyline?.isVisible = true
            invalidate()
        }
    }

    override fun hidePolyline(polyline: BeansPolyline) {
        if (polyline is OSMPolyline) {
            var osmPolyline = polyline as OSMPolyline
            osmPolyline.polyline?.isVisible = false
            invalidate()
        }
    }

    override fun removePolyline(polyline: BeansPolyline) {
        if (polyline is OSMPolyline) {
            var osmPolyline = polyline as OSMPolyline
            overlays.remove(osmPolyline.polyline)
            invalidate()
        }
    }


    override fun fromScreenLocation(point: Point): ai.beans.common.pojo.GeoPoint {
        var loc = projection!!.fromPixels(point.x, point.y)
        return ai.beans.common.pojo.GeoPoint(loc.latitude, loc.longitude)
    }


    override fun getCurrentZoomLevel(): Float {
        return zoomLevelDouble.toFloat()
    }

    override fun setCurrentZoomLevel(zoom: Float) {
        controller.setZoom(zoom.toDouble())
    }

    override fun onDetach() {
        super.onDetach()
        isMapReady.value = false
    }

    override fun clearMapContents() {
        overlayManager.clear()
    }

    override fun setLocationProvider(locationSource: LocationSource) {
    }

    override fun onLifecycleOwnerCreate(savedBundle: Bundle?) {

    }

    override fun onLifecycleOwnerStart() {
        Log.d("OSMMap", "onLifecycleOwnerResume")
    }

    override fun onLifecycleOwnerResume() {
        onResume()
        Log.d("OSMMap", "onLifecycleOwnerResume")
    }

    override fun onLifecycleOwnerPause() {
        onPause()
    }

    override fun onLifecycleOwnerStop() {
        //onDetach()
        //isMapReady.value = false
        Log.d("OSMMap", "onLifecycleOwnerStop")
    }

    override fun onLifecycleOwnerDestroy() {

    }

    override fun onLowMemoryWarning() {

    }

    override fun isSatelliteViewEnabled(): Boolean {
        return false
    }

    override fun enableSatelliteView(shouldEnableSatellite: Boolean) {
        // Not available in offline maps
    }

    override fun getTileInfoForCurrentBounds(minZoom: Int, maxZoom: Int): BeansTileInfo? {
        var tileInfo = BeansTileInfo()
        tileInfo.availableInternalStorage = tileManager!!.cacheCapacity().toDouble()

        val tilecount: Int = tileManager!!.possibleTilesInArea(boundingBox, minZoom, maxZoom)
        tileInfo.estimatedNumberOfTiles = tilecount.toDouble()

        return tileInfo
    }

    override fun downloadTilesForVisibleRegion(context: Context, minZoom: Int, maxZoom: Int) {
        //this triggers the download


        //this triggers the download
        tileManager!!.downloadAreaAsync(
            context,
            boundingBox,
            minZoom,
            maxZoom,
            object : CacheManagerCallback {
                override fun onTaskComplete() {
                    Toast.makeText(context, "Download complete!", Toast.LENGTH_LONG).show()
                }

                override fun onTaskFailed(errors: Int) {
                    Toast.makeText(
                        context,
                        "Download complete with $errors errors",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun updateProgress(
                    progress: Int,
                    currentZoomLevel: Int,
                    zoomMin: Int,
                    zoomMax: Int
                ) {
                    //NOOP since we are using the build in UI
                }

                override fun downloadStarted() {
                    //NOOP since we are using the build in UI
                }

                override fun setPossibleTilesInArea(total: Int) {
                    //NOOP since we are using the build in UI
                }
            })

    }

}