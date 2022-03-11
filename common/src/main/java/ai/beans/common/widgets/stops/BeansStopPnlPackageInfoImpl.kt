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

    var data1 : TextView ?= null
    var pickupTime : TextView ?= null

    var dropOffContainer : RelativeLayout ?= null
    var pickupContainer : RelativeLayout ?= null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_pnl_status, this, true) as RelativeLayout
        data1 = v.findViewById(R.id.data1)
        pickupTime = v.findViewById(R.id.pickupWindow)

        dropOffContainer = v.findViewById(R.id.deliveryContainer)
        pickupContainer = v.findViewById(R.id.pickupContainer)

    }

    fun render(currentStop: RouteStop?) {
        currentStop?.let {
            if(it.type == RouteStopType.DROPOFF) {
                dropOffContainer?.visibility = View.VISIBLE
                pickupContainer?.visibility = View.GONE
                if (it.has_apartments) {
                    data1?.text = (it.apartment_count ?: 0).toString() + " x " + (it.total_package_count ?: 0).toString()
                } else {
                    data1?.text = (it.tracking_id ?: "") + " x " + (it.num_packages ?: 0).toString()
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