package ai.beans.common.maps.renderers

import ai.beans.common.pojo.search.MapMarkerType

data class CachedPinMovement(val listItemId: String, val lat: Double?, val lng: Double?, val type: MapMarkerType?) {
}