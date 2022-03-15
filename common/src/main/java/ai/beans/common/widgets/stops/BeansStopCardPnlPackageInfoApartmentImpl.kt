package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStopType
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat

class BeansStopCardPnlPackageInfoApartmentImpl : RelativeLayout {

    var sid : TextView ?= null
    var numPackages : TextView ?= null
    var dropOffSIDContainer : ConstraintLayout ?= null
    var dropOffItemsContainer : ConstraintLayout ?= null
    var pickupContainer : ConstraintLayout ?= null
    var pickupTime : TextView ?= null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_pnl_status_apartment, this, true) as RelativeLayout
        sid = v.findViewById(R.id.sid)
        numPackages = v.findViewById(R.id.package_count)
        pickupTime = v.findViewById(R.id.pickupWindow)
        dropOffSIDContainer = v.findViewById(R.id.sid_container)
        dropOffItemsContainer = v.findViewById(R.id.items_container)
        pickupContainer = v.findViewById(R.id.pickup_container)

    }

    fun render(currentStop: RouteStop?) {
//        currentStop?.let {
//            sid?.text = it.tracking_id
//            if(it.num_packages != null) {
//                numPackages?.text = it.num_packages.toString()
//            }
//        }
        currentStop?.let {
            if(it.type == RouteStopType.DROPOFF) {
                dropOffItemsContainer?.visibility = View.VISIBLE
                dropOffSIDContainer?.visibility = View.VISIBLE
                pickupContainer?.visibility = View.GONE
                if (it.tracking_id != null) {
                    sid?.text = it.tracking_id
                } else {
                    sid?.text = ""
                }
                numPackages?.text = "x" + (it.num_packages ?: 0).toString()
            } else if(it.type == RouteStopType.PICKUP ) {
                dropOffItemsContainer?.visibility = View.GONE
                dropOffSIDContainer?.visibility = View.GONE
                pickupContainer?.visibility = View.VISIBLE
                if(it.deliver_from_str != null && it.deliver_by_str != null) {
                    var originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm");
                    val displayFormat = SimpleDateFormat("HH:mm")
                    var fromDate = originalFormat.parse(it.deliver_from_str)
                    var toDate = originalFormat.parse(it.deliver_by_str)

                    var displayFromStr = displayFormat.format(fromDate).toString()
                    var displayByStr = displayFormat.format(toDate).toString()

                    var str = displayFromStr + " - " + displayByStr
                    pickupTime?.text = str
                } else {
                    pickupTime?.text = "All Day"
                }
            }
        }


    }


}