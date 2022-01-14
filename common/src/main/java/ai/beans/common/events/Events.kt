package ai.beans.common.events

import ai.beans.common.pojo.RouteStop
import ai.beans.common.pojo.RouteStopStatus
import com.google.android.gms.maps.model.Marker

class MarkerRenderComplete(val marker: Marker, val isNewMarker:Boolean?=false)

class UpdateStopStatus(val stopId : String, val status: RouteStopStatus)
class RenderStopCard(val stop : RouteStop)
class ResetStopViews()
class RefreshRoute(val resetViews : Boolean = false)
class RouteErrorDetected()
class GetAllStopsFromServer

class ExpandCard
class CollapseCard
class ShowCard
class HideCard

class ChangeSelectedStop

//Card events
class GoToPreviousStop
class GoToNextStop
class ShowNextStop
class ShowPrevStop