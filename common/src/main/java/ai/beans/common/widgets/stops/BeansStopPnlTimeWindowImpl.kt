package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.common.util.Strings

class BeansStopPnlTimeWindowImpl : RelativeLayout {
    var timeWindowTextView : TextView?= null

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
            .inflate(R.layout.beans_stop_pnl_time_window, this, true) as RelativeLayout

        timeWindowTextView = v.findViewById(R.id.time_window)
    }

    fun render(routeStop: RouteStop?) {
        routeStop?.let {
            if (!Strings.isEmptyOrWhitespace(it.deliver_from_str) && !Strings.isEmptyOrWhitespace(it.deliver_by_str)) {
                visibility = View.VISIBLE
                var deliver_from_str = it.deliver_from_str
                if (deliver_from_str?.indexOf(" ") !== -1) {
                    deliver_from_str = deliver_from_str?.substring(deliver_from_str?.indexOf(" ") + 1)
                }
                var deliver_by_str = it.deliver_by_str
                if (deliver_by_str?.indexOf(" ") !== -1) {
                    deliver_by_str = deliver_by_str?.substring(deliver_by_str?.indexOf(" ") + 1)
                }
                timeWindowTextView?.text = deliver_from_str + " - " + deliver_by_str
            } else {
                visibility = View.GONE
            }
        }
    }
}