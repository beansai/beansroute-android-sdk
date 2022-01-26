package ai.beans.common.pojo.search

import ai.beans.common.pojo.GeoPoint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.google.maps.android.ui.IconGenerator
import ai.beans.common.pojo.IconItem
import ai.beans.common.widgets.BeansMarkerIconV2

class MarkerInfo {
    var id: Int = 0
    var location: GeoPoint ?= null
    var route_point_type: MapMarkerType ?= null
    var type: MapMarkerType ?= null
    var status: String ?= null
    var text: String ?= null

    suspend fun getMarkerIconV2(iconGenerator: IconGenerator, context: Context, iconData:IconItem?): Bitmap? {
        val TRANSPARENT_DRAWABLE = ColorDrawable(Color.TRANSPARENT)
        if(iconData != null) {
            //Big marker
            var marker = BeansMarkerIconV2(context)
            marker.setMarkerContent(iconData, text)
            iconGenerator.setContentView(marker)
            iconGenerator.setBackground(TRANSPARENT_DRAWABLE)
            return iconGenerator.makeIcon()
        }
        return null
    }
}