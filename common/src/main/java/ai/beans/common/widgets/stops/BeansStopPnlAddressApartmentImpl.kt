package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import ai.beans.common.widgets.markers.IconAttributes
import ai.beans.common.widgets.markers.MarkerIconHelper
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.maps.android.ui.IconGenerator
import org.greenrobot.eventbus.EventBus

class BeansStopPnlAddressApartmentImpl : RelativeLayout {

    var buttonsHidden: Boolean = false
    var address: TextView? = null
    var name: TextView? = null
    var iconGenerator: IconGenerator? = null
    var marker_container: View? = null
    var showMarkersWithSidColor = false

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
        iconGenerator = IconGenerator(context)
        iconGenerator?.setBackground(ColorDrawable(Color.TRANSPARENT))

        val v = LayoutInflater.from(context).inflate(
            R.layout.beans_stop_pnl_address_apartment,
            this,
            true
        ) as RelativeLayout

        address = v.findViewById(R.id.address_line1)
        name = v.findViewById(R.id.unit)
        marker_container = v.findViewById(R.id.marker)

        showMarkersWithSidColor = false
    }

    fun render(routeStop: RouteStop?) {
        routeStop?.let {
            address?.text = it.address
            name?.text = it.unit
            renderMarker(routeStop)
        }
    }

    protected fun renderMarker(routeStop: RouteStop?) {
        routeStop?.let {
            var attributes = IconAttributes()
            attributes.status = routeStop.status
            attributes.showMarkerWithTextLabel = false
            attributes.type = routeStop.type
            attributes.hasApartments = routeStop.has_apartments
            attributes.showSelected = false
            attributes.number = routeStop.route_display_number
            attributes.sid = routeStop.tracking_id
            attributes.useSidColors = showMarkersWithSidColor

            var bitmap =
                MarkerIconHelper().getMarkerIconBitmap(context, iconGenerator!!, attributes)

            var bmpDrawable = BitmapDrawable(resources, bitmap)
            marker_container?.background = bmpDrawable
        }
    }

    fun setBus(localBus: EventBus) {
    }
}