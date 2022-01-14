package ai.beans.common.maps.polylines

import com.google.android.gms.maps.model.Polyline

class GooglePolyline : BeansPolyline {
    var polyline : Polyline? = null

    constructor(polyline : Polyline) {
        this.polyline = polyline
    }
}