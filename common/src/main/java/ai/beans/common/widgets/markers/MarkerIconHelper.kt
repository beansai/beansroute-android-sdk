package ai.beans.common.widgets.markers

import ai.beans.common.pojo.RouteStopType
import ai.beans.common.utils.MapMarkerBitmapCache
import android.content.Context
import android.graphics.Bitmap
import android.widget.RelativeLayout
import com.google.maps.android.ui.IconGenerator

class MarkerIconHelper {
    fun getMarkerIconBitmap(context: Context, iconGenerator: IconGenerator, attributes: IconAttributes) : Bitmap {
        var bitmapKey = generateKey(attributes)
        var bitmap = MapMarkerBitmapCache.bitmapHashMap[bitmapKey]
        if (bitmap != null) {
            return bitmap
        } else {
            var markerLayout : RelativeLayout ?= null
            if(attributes.hasApartments) {
                markerLayout = BeansStopMarkerApartmentImpl(context)
                bitmap = markerLayout.getIconBitmap(iconGenerator, attributes)
            } else {
                when (attributes.type) {
                    RouteStopType.DROPOFF -> {
                        markerLayout = BeansStopMarkerDropoffImpl(context)
                        bitmap = markerLayout.getIconBitmap(iconGenerator, attributes)
                    }
                    RouteStopType.PICKUP -> {
                        markerLayout = BeansStopMarkerPickupImpl(context)
                        bitmap = markerLayout.getIconBitmap(iconGenerator, attributes)
                    }
                }
            }

            MapMarkerBitmapCache.bitmapHashMap[bitmapKey] = bitmap!!
            return bitmap
        }
    }

    private fun generateKey( attributes: IconAttributes): String {
        var stringKey = attributes.type.toString()
        stringKey += attributes.status.toString()
        if(attributes.hasApartments) {
            stringKey += "hasApt"
        }
        if(attributes.showSelected) {
            stringKey += "Selected"
        }
        if(attributes.number != null) {
            stringKey += attributes.number.toString()
        }

        if(attributes.showMarkerWithTextLabel) {
            if (attributes.addressString != null) {
                stringKey += attributes.addressString
            }
        }

        if(attributes.useSidColors) {
            var color = getSIDColor(attributes.sid)
            stringKey += color
        }

        return stringKey
    }

}