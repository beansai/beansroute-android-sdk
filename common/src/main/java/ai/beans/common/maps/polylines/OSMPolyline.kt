package ai.beans.common.maps.polylines

import org.osmdroid.views.overlay.Polyline

class OSMPolyline : BeansPolyline {
    var polyline : Polyline? = null

    constructor(polyline : Polyline) {
        this.polyline = polyline
    }
}