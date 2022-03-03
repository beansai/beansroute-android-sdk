package ai.beans.common.events

import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.search.FeedbackNoteType
import ai.beans.common.pojo.search.MapMarkerType
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

class PinMoved(val listItemId: String, val lat: Double?, val lng: Double?, val type: MapMarkerType?)

class ShowDataEntryDialog(val type: FeedbackNoteType, val note:String? = null)