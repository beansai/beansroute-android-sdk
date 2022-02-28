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
    var infoButton : ImageButton ?= null
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
        infoButton = v.findViewById(R.id.infoButton)
        prevStopButton = v.findViewById(R.id.prevButton)
        nextStopButton = v.findViewById(R.id.nextButton)
    }

    fun render(currentStop: RouteStop?) {
        this.currentStop = currentStop
        if (!buttonsHidden) {
            visibility = View.VISIBLE
            if (currentStop!!.has_apartments) {
                //Apt parent stop
                infoButton?.visibility = View.VISIBLE
                navigateButton?.visibility = View.VISIBLE
                btnDeliveredImpl?.visibility = View.GONE
                btnAttemptedImpl?.visibility = View.GONE

            } else if (currentStop!!.parent_list_item_id != null && currentStop!!.parent_list_item_id != "") {
                //Apt child stop
                infoButton?.visibility = View.GONE
                navigateButton?.visibility = View.VISIBLE
                btnDeliveredImpl?.visibility = View.VISIBLE
                btnAttemptedImpl?.visibility = View.GONE
            } else {
                //Single family stop
                infoButton?.visibility = View.VISIBLE
                navigateButton?.visibility = View.VISIBLE
                btnDeliveredImpl?.visibility = View.VISIBLE
                btnAttemptedImpl?.visibility = View.GONE
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
                infoButton?.visibility = View.GONE
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