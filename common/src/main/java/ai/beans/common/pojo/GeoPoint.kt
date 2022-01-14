package ai.beans.common.pojo

import android.content.Context

class GeoPoint {
    var lat: Double? = 0.0
    var lng: Double? = 0.0

    constructor() {
    }

    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }

    override fun toString(): String {
        return lat.toString() + "," + lng.toString()
    }

    fun latitudeAsString(): String {
        return lat.toString()
    }

    fun longitudeAsString(): String {
        return lng.toString()
    }

}