package ai.beans.common.widgets.markers

import ai.beans.common.R
import ai.beans.common.pojo.RouteStopStatus
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.maps.android.ui.IconGenerator

class BeansStopMarkerPickupImpl: RelativeLayout {

    companion object {
    }

    var containerSmall: ConstraintLayout? = null
    var pinColorLayerSmall: View? = null
    var stopNumberSmall: TextView? = null
    var crossSmall: View? = null
    var checkSmall: View? = null

    var containerLarge: ConstraintLayout? = null
    var pinColorLayerLarge: View? = null
    var stopNumberLarge: TextView? = null
    var crossLarge: View? = null
    var checkLarge: View? = null

    var iconLabelText : TextView ?= null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    init {
        val v = LayoutInflater.from(context)
            .inflate(R.layout.beans_stop_marker_pickup, this, true) as RelativeLayout

        containerSmall = v.findViewById(R.id.pickup_container_small)
        pinColorLayerSmall = v.findViewById(R.id.pinColorLayerSmall)
        stopNumberSmall = v.findViewById(R.id.text_number_small)
        crossSmall = v.findViewById(R.id.cross_small)
        checkSmall = v.findViewById(R.id.check_small)

        containerLarge = v.findViewById(R.id.pickup_container_large)
        pinColorLayerLarge = v.findViewById(R.id.pinColorLayerLarge)
        stopNumberLarge = v.findViewById(R.id.text_number_large)
        crossLarge = v.findViewById(R.id.cross_large)
        checkLarge = v.findViewById(R.id.check_large)

        iconLabelText = v.findViewById(R.id.icon_label_text)
    }

    fun getIconBitmap(iconGenerator: IconGenerator, attributes : IconAttributes) : Bitmap {
        var container: ConstraintLayout? = null
        var pinColorLayer: View? = null
        var stopNumber: TextView? = null
        var cross: View? = null
        var checkmark: View? = null

        var color: Int? = null
        if (attributes.useSidColors) {
            color = getSIDColor(attributes.sid)
        } else {
            color = getStatusColor(attributes.status)
        }

        if(attributes.showSelected) {
            containerLarge?.visibility = View.VISIBLE
            containerSmall?.visibility = View.GONE
            container = containerLarge
            pinColorLayer = pinColorLayerLarge
            stopNumber = stopNumberLarge
            cross = crossLarge
            checkmark = checkLarge

        } else {
            containerLarge?.visibility = View.GONE
            containerSmall?.visibility = View.VISIBLE
            container = containerSmall
            pinColorLayer = pinColorLayerSmall
            stopNumber = stopNumberSmall
            cross = crossSmall
            checkmark = checkSmall

        }

        //paint checkmark, cross and number in the same color
        var dr = cross!!.background as VectorDrawable
        dr.setTint(Color.WHITE)
        dr = checkmark!!.background as VectorDrawable
        dr.setTint(Color.WHITE)
        stopNumber?.setTextColor(Color.WHITE)

        dr = pinColorLayer!!.background as VectorDrawable
        dr.setTint(color)
        if(attributes.useSidColors) {
            if (attributes.status == RouteStopStatus.FINISHED) {
                checkmark?.visibility = View.VISIBLE
            } else if (attributes.status == RouteStopStatus.FAILED) {
                cross?.visibility = View.VISIBLE
            } else {
                //show the stop # if available
                if (attributes.number != null) {
                    stopNumber?.text = attributes.number.toString()
                }
            }
        } else {
            //we are showing colors based on status....show numbers if needed
            if (attributes.number != null) {
                stopNumber?.text = attributes.number.toString()
            }
        }

        //The icon label text (which appears below the icon) is independant of any other attributes
        if(attributes.addressString != null && attributes.showMarkerWithTextLabel) {
            iconLabelText?.visibility = View.VISIBLE
            iconLabelText?.text = attributes.addressString
        }

        iconGenerator.setContentView(this)
        var bitmap = iconGenerator.makeIcon()
        return bitmap

    }
}