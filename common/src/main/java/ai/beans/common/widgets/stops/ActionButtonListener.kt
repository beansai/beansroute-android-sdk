package ai.beans.common.widgets.stops

import ai.beans.common.pojo.RouteStop

interface ActionButtonListener {
    fun onNavigateClicked(currentStop: RouteStop?)
    fun onDeliveredClicked(currentStop: RouteStop?)
    fun onAttemptedClicked(currentStop: RouteStop?)
    fun onPrevStopClicked(currentStop: RouteStop?)
    fun onNextStopClicked(currentStop: RouteStop?)
}
