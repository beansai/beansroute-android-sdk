package ai.beans.common.maps

import ai.beans.common.maps.boundingbox.BeansMapBoundingBox
import ai.beans.common.maps.mapproviders.BeansMapViewListener
import ai.beans.common.maps.markers.BeansMarkerAttributes
import ai.beans.common.maps.markers.BeansMarkerInterface
import ai.beans.common.maps.polylines.BeansPolyline
import ai.beans.common.maps.tiledownlader.BeansTileInfo
import ai.beans.common.pojo.GeoPoint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Location
import androidx.lifecycle.MutableLiveData

interface BeansMapFragmentInterface {
    fun registerMapEventListener(rendererId: String, listener: BeansMapViewListener): kotlin.Unit
    fun getMapReadyFlag(): MutableLiveData<Boolean>
    fun isMapReady(): Boolean

    //Marker stuff
    fun createMarker(attributes: BeansMarkerAttributes): BeansMarkerInterface
    fun removeMarker(marker: BeansMarkerInterface)
    fun showMarker(marker: BeansMarkerInterface)
    fun hideMarker(marker: BeansMarkerInterface)
    fun updateMarkerIcon(marker: BeansMarkerInterface, icon: Bitmap)

    //Polyline stuff
    fun createPolyline(points: ArrayList<GeoPoint>): BeansPolyline?
    fun showPolyline(polyline: BeansPolyline)
    fun hidePolyline(polyline: BeansPolyline)
    fun removePolyline(polyline: BeansPolyline)

    //Satellite view
    fun enableSatelliteView(shouldEnableSatellite: Boolean)
    fun isSatelliteViewEnabled(): Boolean

    fun setCurrentLocation(location: Location, zoomLevel: Float? = null, animate: Boolean = true)
    fun setCurrentBounds(
        points: ArrayList<GeoPoint>,
        width: Int,
        height: Int,
        padding: Int = 0,
        animate: Boolean = false
    )

    fun getCurrentZoomLevel(): Float
    fun fromScreenLocation(point: Point): GeoPoint
    fun getBoundsForLocations(points: ArrayList<GeoPoint>): BeansMapBoundingBox
    fun getVisibleMapBounds(): BeansMapBoundingBox

    //Tile info and tile downloading
    fun getTileInfoForCurrentBounds(minZoom: Int, maxZoom: Int): BeansTileInfo?
    fun downloadTilesForVisibleRegion(context: Context, minZoom: Int, maxZoom: Int)
}