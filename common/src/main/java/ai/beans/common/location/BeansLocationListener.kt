package ai.beans.common.location

import android.location.Location

interface BeansLocationListener {
    var thresholdInMeters : Int
    var prevLocation : Location?
    fun locationChanged(location : Location)
}