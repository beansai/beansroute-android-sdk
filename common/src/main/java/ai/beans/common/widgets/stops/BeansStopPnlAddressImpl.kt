package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.maps.android.ui.IconGenerator
import org.greenrobot.eventbus.EventBus

class BeansStopPnlAddressImpl : RelativeLayout {

    var iconGenerator : IconGenerator ?= null
    var marker_container : View ?= null
    var buttonsHidden : Boolean = false
    var address: TextView ?= null
    var name: TextView ?= null
    var note: TextView ?= null
    var unit: TextView ?= null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_pnl_address, this, true) as RelativeLayout
        address = v.findViewById(R.id.address_line1)
        //name = v.findViewById(R.id.name)
        //note = v.findViewById(R.id.notes)
        unit = v.findViewById(R.id.unit)
    }

    fun render(routeStop: RouteStop?) {
        routeStop?.let {
            address?.text = it.address

            if(it.unit != null && it.unit!!.isNotEmpty()) {
                unit?.text = "Unit " + it.unit
                unit?.visibility = View.VISIBLE
            }
            else {
                unit?.visibility = View.GONE
            }
        }
    }

    fun setBus(localBus: EventBus) {

    }
}