package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

class BeansStopPnlNotesImpl : RelativeLayout {
    var note: TextView?= null
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context)
            .inflate(R.layout.beans_stop_pnl_notes, this, true) as RelativeLayout
        note = v.findViewById(R.id.notes)
    }

    fun render(routeStop: RouteStop?) {
        routeStop?.let {
            //Customer Name
//            if(it.customer_name != null && it.customer_name!!.isNotEmpty()) {
//                name?.text = it.customer_name
//                name?.visibility = View.VISIBLE
//            }
//            else {
//                name?.visibility = View.GONE
//            }

            //Notes
            if(it.notes != null && it.notes!!.isNotEmpty()) {
                note?.text = it.notes
                note?.visibility = View.VISIBLE
            }
            else {
                note?.visibility = View.GONE
            }
        }
    }

}