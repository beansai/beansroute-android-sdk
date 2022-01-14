package ai.beans.common.widgets.stops

import ai.beans.common.pojo.RouteStop
import ai.beans.common.R
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ChildStopViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    var stop: RouteStop ?= null
    var actionButtonListener : ActionButtonListener ?= null
    var aptCardApartmentImpl : BeansStopCardApartmentImpl ?= null

    init {
        aptCardApartmentImpl = itemView.findViewById(R.id.stop_card)
    }

    fun render() {
        aptCardApartmentImpl?.renderStop(stop)
        aptCardApartmentImpl?.showConnectorLine()
    }

    fun setActionListener(actionButtonListener: ActionButtonListener?) {
        this.actionButtonListener = actionButtonListener
        aptCardApartmentImpl?.actionButtonListener = this.actionButtonListener
    }
}