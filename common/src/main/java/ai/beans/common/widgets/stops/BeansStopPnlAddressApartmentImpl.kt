package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import org.greenrobot.eventbus.EventBus

class BeansStopPnlAddressApartmentImpl : RelativeLayout {

    var buttonsHidden: Boolean = false
    var address: TextView? = null
    var name: TextView? = null

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
        val v = LayoutInflater.from(context).inflate(
            R.layout.beans_stop_pnl_address_apartment,
            this,
            true
        ) as RelativeLayout



        address = v.findViewById(R.id.address_line1)
        name = v.findViewById(R.id.unit)


    }

    fun render(routeStop: RouteStop?) {
        routeStop?.let {
            address?.text = it.address
            name?.text = it.unit
        }
    }


    fun setBus(localBus: EventBus) {

    }
}