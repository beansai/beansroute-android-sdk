package ai.beans.common

import ai.beans.common.maps.markers.BeansMarkerInterface
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker

object DummyEvent
object LoginEvent
object SourceCamera
object SourceGallery
//Map interaction events
class MapClicked(val location : LatLng)
class MapLongClicked(val location : LatLng)
class MarkerClicked(val marker : BeansMarkerInterface)
class MarkerDragged(val marker : BeansMarkerInterface)
class MarkerDragStart(val marker : BeansMarkerInterface)
class MarkerDragEnd(val marker : BeansMarkerInterface)
class MapMoved(
    val bounds: LatLngBounds?,
    val oldCenter: LatLng?,
    val newCenter: LatLng?,
    val distanceMoved: Double?
)
class MarkerInfoWindowClicked(val marker : BeansMarkerInterface)

//Map affordance visibility control events
class ShowSearchBar
class HideSearchBar
class ShowBackButton
class HideBackButton
class ShowShareButton
class HideShareButton
class ServerDataUpdated

//Camera picture capture event
class PictureCaptured(val file : String)

//Defered Push Data
class DeferredPushNotification(val data: String)

//PushToken saving to server
class SavePushTokenToServer



