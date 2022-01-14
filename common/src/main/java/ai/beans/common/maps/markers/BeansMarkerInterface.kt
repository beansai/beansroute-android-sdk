package ai.beans.common.maps.markers

import ai.beans.common.pojo.GeoPoint
import android.graphics.Bitmap
import android.location.Location

interface BeansMarkerInterface {
    fun setMarkerTag(tag : Object)
    fun getMarkerTag() : Object?
    fun showMarker()
    fun hideMarker()
    fun showOnTop(onTop : Boolean = true)
    //fun setIcon(icon : Bitmap)
    fun getLocation() : GeoPoint
    fun setLocation(location: GeoPoint)
}