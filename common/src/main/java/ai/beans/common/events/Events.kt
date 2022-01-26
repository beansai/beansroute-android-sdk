package ai.beans.common.events

import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.search.FeedbackNoteType
import com.google.android.gms.maps.model.Marker

class MarkerRenderComplete(val marker: Marker, val isNewMarker:Boolean?=false)

class UpdateStopStatus(val stopId : String, val status: RouteStopStatus)
class ResetStopViews()

class ExpandCard
class CollapseCard
class ShowCard
class HideCard

//Card events
class ShowMoreInfo
class GoToPreviousStop
class GoToNextStop
class ShowNextStop
class ShowPrevStop

class ShowDataEntryDialog(val type: FeedbackNoteType, val note:String? = null)