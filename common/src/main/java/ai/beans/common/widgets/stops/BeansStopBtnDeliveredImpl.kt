package ai.beans.common.widgets.stops

import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout

class BeansStopBtnDeliveredImpl : RelativeLayout {

    var button : RelativeLayout ?= null
    var selected : RelativeLayout ?= null
    var unselected : RelativeLayout ?= null
    var isButtonSet = false

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
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_btn_delivered,
                                    this,
                            true) as RelativeLayout

        button = v.findViewById(R.id.deliveredButtonContainer)
        selected = v.findViewById(R.id.selected_container)
        unselected = v.findViewById(R.id.unselected_container)
    }

    fun setSelectedState(isOneButton: Boolean, isDelivered: Boolean, isAttempted: Boolean) {
        if(isDelivered) {
            button?.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_card_action_button_delivered))
            selected?.visibility = View.VISIBLE
            unselected?.visibility = View.GONE
            isButtonSet = true
        } else if (isOneButton && isAttempted) {
            // Not delivered, but attempted, one button
            button?.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_card_action_button_attempted))
            selected?.visibility = View.VISIBLE
            unselected?.visibility = View.GONE
            isButtonSet = true
        } else {
            // Not delivered, and either attempted or two buttons
            button?.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_card_action_button_unselected))
            selected?.visibility = View.GONE
            unselected?.visibility = View.VISIBLE
            isButtonSet = false
        }
    }

}