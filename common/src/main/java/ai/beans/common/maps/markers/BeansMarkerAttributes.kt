package ai.beans.common.maps.markers

import ai.beans.common.pojo.GeoPoint
import android.graphics.Bitmap
import android.graphics.PointF
import com.google.android.gms.maps.model.LatLng

class BeansMarkerAttributes {
    var isVisible = true
    var location : GeoPoint ?= null
    var bitmap : Bitmap?= null
    var anchor : PointF ?= null
    var isDraggable = false
}