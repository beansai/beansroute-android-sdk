package ai.beans.common.maps.mapproviders

import ai.beans.common.maps.markers.BeansMarkerInterface
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

interface BeansMapViewListener {
    fun mapMoved(
        bounds: LatLngBounds?,
        oldCenter: LatLng?,
        newCenter: LatLng?,
        distanceMoved: Double?
    )

    fun mapReady()
    fun onMapClicked(location: LatLng)
    fun onMapLongClicked(location: LatLng)
    fun onMapMarkerClicked(marker: BeansMarkerInterface)
    fun onMarkerDragStart(marker: BeansMarkerInterface?)
    fun onMarkerDragEnd(marker: BeansMarkerInterface?)
    fun onMarkerDrag(marker: BeansMarkerInterface?)
    fun onInfoWindowClicked(marker: BeansMarkerInterface)
}