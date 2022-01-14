package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout

class BeansStopPnlActionsImpl : RelativeLayout {

    var btnDeliveredImpl: BeansStopBtnDeliveredImpl? = null
    var btnAttemptedImpl: BeansStopBtnAttemptedImpl? = null
    var navigateButton: ImageButton? = null
    var currentStop: RouteStop? = null
    var buttonsHidden: Boolean = true
    var prevStopButton: Button? = null
    var nextStopButton: Button? = null

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
            .inflate(R.layout.beans_stop_pnl_actions, this, true) as RelativeLayout
        btnDeliveredImpl = v.findViewById(R.id.deliveredButton)
        btnAttemptedImpl = v.findViewById(R.id.attemptedButton)
        navigateButton = v.findViewById(R.id.navigateActionButton)
        prevStopButton = v.findViewById(R.id.prevButton)
        nextStopButton = v.findViewById(R.id.nextButton)
    }

    fun render(currentStop: RouteStop?) {
        this.currentStop = currentStop
        if (!buttonsHidden) {
            visibility = View.VISIBLE
            if (currentStop!!.has_apartments) {
                //Apt parent stop
                navigateButton?.visibility = View.VISIBLE
                btnDeliveredImpl?.visibility = View.GONE
                btnAttemptedImpl?.visibility = View.GONE

            } else if (currentStop!!.parent_list_item_id != null) {
                //Apt child stop
                navigateButton?.visibility = View.VISIBLE
                btnDeliveredImpl?.visibility = View.VISIBLE
                btnAttemptedImpl?.visibility = View.VISIBLE
            } else {
                //Single family stop
                navigateButton?.visibility = View.VISIBLE
                btnDeliveredImpl?.visibility = View.VISIBLE
                btnAttemptedImpl?.visibility = View.VISIBLE
            }
        }

        currentStop?.let {
            if (it.status == RouteStopStatus.FINISHED) {
                btnDeliveredImpl?.setSelectedState(true)
                btnAttemptedImpl?.setSelectedState(false)
            } else if (it.status == RouteStopStatus.FAILED) {
                btnDeliveredImpl?.setSelectedState(false)
                btnAttemptedImpl?.setSelectedState(true)
            } else {
                btnDeliveredImpl?.setSelectedState(false)
                btnAttemptedImpl?.setSelectedState(false)
            }

            if (it.status == RouteStopStatus.NOLOCATION) {
                navigateButton?.visibility = View.GONE
            }
        }
    }

    fun hide() {
        visibility = View.GONE
        buttonsHidden = true
    }

    fun show() {
        visibility = View.VISIBLE
        buttonsHidden = false
    }

    fun showPrevNextButtons() {
        prevStopButton?.visibility = View.VISIBLE
        nextStopButton?.visibility = View.VISIBLE
    }

    fun hidePrevNextButtons() {
        prevStopButton?.visibility = View.GONE
        nextStopButton?.visibility = View.GONE
    }
}