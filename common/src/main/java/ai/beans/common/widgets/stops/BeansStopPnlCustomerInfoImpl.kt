package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class BeansStopPnlCustomerInfoImpl : RelativeLayout {
    var phoneNumber : String ?= null
    var greyPhone : ImageView?= null
    var bluePhone : ImageView?= null
    var nameTextView : TextView?= null

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
            .inflate(R.layout.beans_stop_pnl_customer_info, this, true) as RelativeLayout

        greyPhone = v.findViewById(R.id.phone_gray)
        bluePhone = v.findViewById(R.id.phone_blue)
        nameTextView = v.findViewById(R.id.user_name)

        setOnClickListener {
            if(phoneNumber != null) {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
                    context.startActivity(intent)
                } catch (ex: Exception) {

                }
            }
        }
    }

    fun render(routeStop: RouteStop?) {
        routeStop?.let {
            if(it.customer_name != null) {
                visibility = View.VISIBLE
                nameTextView?.text = it.customer_name
            } else if(it.customer_phone != null) {
                visibility = View.VISIBLE
                nameTextView?.text = it.customer_phone
            } else {
                nameTextView?.text = null
                visibility = View.GONE
            }

            if(it.customer_phone != null) {
                phoneNumber = it.customer_phone
                bluePhone?.visibility = View.VISIBLE
                greyPhone?.visibility = View.GONE
            } else {
                phoneNumber = null
                bluePhone?.visibility = View.GONE
                greyPhone?.visibility = View.VISIBLE
            }

        }
    }
}