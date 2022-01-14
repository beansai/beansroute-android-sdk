package ai.beans.common.maps.mapproviders

import ai.beans.common.maps.boundingbox.BeansMapBoundingBox
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.maps.polylines.BeansPolyline
import ai.beans.common.maps.tiledownlader.BeansTileInfo
import ai.beans.common.pojo.GeoPoint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.LocationSource

interface BeansMapInterface {
    fun registerMapEventListener(listener: BeansMapViewListener)
    fun createMarker(attribs: BeansMarkerAttributes): BeansMarkerInterface
    fun removeMarker(markerInterface: BeansMarkerInterface)
    fun showMarker(markerInterface: BeansMarkerInterface)
    fun hideMarker(markerInterface: BeansMarkerInterface)
    fun updateMarkerIcon(markerInterface: BeansMarkerInterface, icon: Bitmap)
    fun getCurrentLocation(): GeoPoint?
    fun setCurrentLocation(loc: GeoPoint, zoomLevel: Float? = null, animate: Boolean = true)
    fun setCurrentBounds(
        points: ArrayList<GeoPoint>,
        width: Int,
        height: Int,
        padding: Int = 0,
        animate: Boolean = false
    )

    fun getVisibleMapBounds(): BeansMapBoundingBox
    fun getCurrentZoomLevel(): Float
    fun setCurrentZoomLevel(zoom: Float)
    fun clearMapContents()
    fun fromScreenLocation(point: Point): GeoPoint
    fun getBoundsForLocations(points: ArrayList<GeoPoint>): BeansMapBoundingBox
    fun getMapReadyFlag(): MutableLiveData<Boolean>

    //polylines
    fun createPolyline(points: ArrayList<GeoPoint>): BeansPolyline?
    fun showPolyline(polyline: BeansPolyline)
    fun hidePolyline(polyline: BeansPolyline)
    fun removePolyline(polyline: BeansPolyline)

    //Satellite view
    fun enableSatelliteView(shouldEnableSatellite: Boolean)
    fun isSatelliteViewEnabled(): Boolean

    //Location provider
    fun setLocationProvider(locationSource: LocationSource)

    //Lifecycle methods
    fun onLifecycleOwnerCreate(savedBundle: Bundle?)
    fun onLifecycleOwnerStart()
    fun onLifecycleOwnerResume()
    fun onLifecycleOwnerPause()
    fun onLifecycleOwnerStop()
    fun onLifecycleOwnerDestroy()
    fun onLowMemoryWarning()

    //Tile info and tile downloading
    fun getTileInfoForCurrentBounds(minZoom: Int, maxZoom: Int): BeansTileInfo?
    fun downloadTilesForVisibleRegion(context: Context, minZoom: Int, maxZoom: Int)

}