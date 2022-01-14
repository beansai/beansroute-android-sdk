package ai.beans.common.maps.markers

import ai.beans.common.pojo.GeoPoint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class GoogleMapMarker : BeansMarkerInterface{

    var  marker : Marker
    var tag : Object ?= null

    constructor(marker : Marker) {
        this.marker = marker
        this.marker.tag = this
    }

    override fun setMarkerTag(tag: Object) {
        this.tag = tag
    }

    override fun getMarkerTag(): Object? {
        return tag
    }

    override fun showMarker() {
        marker.isVisible = true
    }

    override fun hideMarker() {
        marker.isVisible = false
    }

    override fun showOnTop(onTop : Boolean) {
        if(onTop) {
            marker.zIndex = 100.0f
        } else {
            marker.zIndex = 90.0f
        }
    }

//    override fun setIcon(icon: Bitmap) {
//        val bmpDescriptor = BitmapDescriptorFactory.fromBitmap(icon)
//        marker.setIcon(bmpDescriptor)
//    }

    override fun getLocation(): GeoPoint {
        var location = GeoPoint()
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        return location
    }

    override fun setLocation(location: GeoPoint) {
        marker.position = LatLng(location.lat!!, location.lng!!)
    }


}