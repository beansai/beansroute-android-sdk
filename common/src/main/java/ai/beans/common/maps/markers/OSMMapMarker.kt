package ai.beans.common.maps.markers

import ai.beans.common.R
import ai.beans.common.pojo.GeoPoint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class OSMMapMarker : Marker, BeansMarkerInterface {
    var tag : Object ?= null

    constructor(mapview: MapView, context: Context) : super(mapview, context) {

    }
    constructor(mapview: MapView) : super(mapview) {

    }

    override fun setMarkerTag(tag: Object) {
        this.tag = tag
    }

    override fun getMarkerTag(): Object? {
        return tag
    }

    override fun showMarker() {
        setVisible(true)
    }

    override fun hideMarker() {
        setVisible(false)
    }

    override fun showOnTop(onTop: Boolean) {

    }

//    override fun setIcon(icon: Bitmap) {
//        //setIcon(BitmapDrawable(context, icon))
//    }

    override fun getLocation(): GeoPoint {
        var location = GeoPoint()
        location.lat = position.latitude
        location.lng = position.longitude
        return location
    }

    override fun setLocation(location: GeoPoint) {
        position = org.osmdroid.util.GeoPoint(location.lat!!, location.lng!!)
    }


}