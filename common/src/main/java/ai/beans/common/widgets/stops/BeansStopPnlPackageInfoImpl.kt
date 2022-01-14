package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import ai.beans.common.pojo.RouteStopType
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*


class BeansStopPnlPackageInfoImpl : RelativeLayout {

    var label1 : TextView ?= null
    var label2 : TextView ?= null
    var data1 : TextView ?= null
    var data2 : TextView ?= null
    var placement : TextView ?= null
    var pickupTime : TextView ?= null
    var placementContainer : LinearLayout ?= null

    var dropOffContainer : RelativeLayout ?= null
    var pickupContainer : RelativeLayout ?= null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_pnl_status, this, true) as RelativeLayout
        label1 = v.findViewById(R.id.label1)
        data1 = v.findViewById(R.id.data1)
        label2 = v.findViewById(R.id.label2)
        data2 = v.findViewById(R.id.data2)
        pickupTime = v.findViewById(R.id.pickupWindow)
        placement = v.findViewById(R.id.placement)
        placementContainer = v.findViewById(R.id.placementContainer)

        dropOffContainer = v.findViewById(R.id.deliveryContainer)
        pickupContainer = v.findViewById(R.id.pickupContainer)

    }

    fun render(currentStop: RouteStop?) {
        currentStop?.let {
            if(it.type == RouteStopType.DROPOFF) {
                dropOffContainer?.visibility = View.VISIBLE
                pickupContainer?.visibility = View.GONE
                if (it.has_apartments) {
                    placementContainer?.visibility = GONE
                    label1?.text = "TOTAL STOPS"
                    data1?.text = it.apartment_count.toString()

                    label2?.text = "TOTAL ITEMS"
                    data2?.text = it.total_package_count?.toString()
                } else {
                    label1?.text = "SID"
                    var trackingId = ""
                    if (it.tracking_id != null) {
                        trackingId = it.tracking_id!!
                    }
                    if (trackingId.length <= 6) {
                        data1?.text = trackingId
                    } else {
                        data1?.text = trackingId.substring(trackingId.length - 6)
                    }

                    label2?.text = "ITEMS"
                    data2?.text = it.num_packages?.toString()

                    if(!it.placement.isNullOrEmpty()) {
                        placementContainer?.visibility = View.VISIBLE
                        placement?.text = it.placement
                    } else {
                        placementContainer?.visibility = View.GONE
                    }
                }
            } else if(it.type == RouteStopType.PICKUP ) {
                dropOffContainer?.visibility = View.GONE
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
                } else if(it.deliver_from != null && it.deliver_by != null) {
                    var  fromDate = Date(it.deliver_from!!.toLong())
                    var  toDate = Date(it.deliver_by!!.toLong())

                    if(fromDate != null && toDate != null) {
                        val displayFormat = SimpleDateFormat("HH:mm")
                        var displayFromStr = displayFormat.format(fromDate)
                        var displayByStr = displayFormat.format(toDate).toString()
                        var str = displayFromStr + " - " + displayByStr
                        pickupTime?.text = str
                    } else {
                        pickupTime?.text = "All Day"
                    }

                    /*var originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm");
                    val displayFormat = SimpleDateFormat("HH:mm")
                    var fromDate = originalFormat.parse(it.deliver_from_str)
                    var toDate = originalFormat.parse(it.deliver_by_str)

                    var displayFromStr = displayFormat.format(fromDate).toString()
                    var displayByStr = displayFormat.format(toDate).toString()

                    var str = displayFromStr + " - " + displayByStr
                    pickupTime?.text = str*/
                } else {
                    pickupTime?.text = "All Day"
                }
            }
        }
    }


}