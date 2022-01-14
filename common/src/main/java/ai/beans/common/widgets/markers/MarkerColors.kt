package ai.beans.common.widgets.markers

import ai.beans.common.pojo.RouteStopStatus
import android.graphics.Color
import androidx.core.text.isDigitsOnly
import java.lang.NumberFormatException

fun getSIDColor(sidStr : String?): Int {
    if(sidStr == null) {
        return Color.parseColor("#1E8AD2")
    } else {
        var cleanSidStr = sidStr.replace("\"","")
        cleanSidStr = cleanSidStr.replace(" ","")
        var sidArray = cleanSidStr.split(",")
        //find the first valid numeric SID
        var sid : String ?= null
        for(str in sidArray) {
            if(str.isDigitsOnly()) {
                sid = str
                break
            }
        }
        if(sid == null) {
            return Color.parseColor("#1E8AD2")
        }

        //get the shelf id
        var sidNum = 0
        try {
            sidNum = sid.toInt()
        } catch (excption : NumberFormatException) {
            //The SID is not a valid number
            sidNum
        }

        if(sidNum in 1000..1499) {
            return Color.parseColor("#1CAFFF")
        }

        if(sidNum in 1500..1999) {
            return Color.parseColor("#FD60DF")
        }

        if(sidNum in 2000..2499) {
            return Color.parseColor("#EF5677")

        }

        if(sidNum in 2500..2999) {
            return Color.parseColor("#47D66A")
        }

        if(sidNum in 3000..3499) {
            return Color.parseColor("#FFA01C")
        }

        if(sidNum in 3500..3999) {
            return Color.parseColor("#26C6DF")
        }

        if(sidNum in 4000..4499) {
            return Color.parseColor("#00E3CC")
        }

        if(sidNum in 4500..4999) {
            return Color.parseColor("#6A68DA")
        }
        if(sidNum in 5000..5499) {
            return Color.parseColor("#7F39E1")
        }

        if(sidNum in 5500..5999) {
            return Color.parseColor("#1CFF55")
        }
        if(sidNum in 6000..6499) {
            return Color.parseColor("#1C5CFF")
        }

        if(sidNum in 6500..6999) {
            return Color.parseColor("#FFD738")
        }
        if(sidNum in 7000..7499) {
            return Color.parseColor("#C9F02D")
        }

        if(sidNum in 7500..7999) {
            return Color.parseColor("#FD7755")
        }
        if(sidNum in 8000..8499) {
            return Color.parseColor("#B700FF")
        }

        if(sidNum in 8500..8999) {
            return Color.parseColor("#6CCEFA")
        }

        return Color.parseColor("#1E8AD2")

    }
}

fun getStatusColor(status: RouteStopStatus?) : Int {
    when(status) {
        RouteStopStatus.NEW -> {
            return Color.parseColor("#1A66D2")
        }

        RouteStopStatus.FAILED -> {
            return Color.parseColor("#FC4F2A")
        }

        RouteStopStatus.FINISHED -> {
            return Color.parseColor("#2ABAA7")
        }

        RouteStopStatus.NOLOCATION -> {
            return Color.RED
        }

        RouteStopStatus.MISLOAD -> {
            return Color.DKGRAY
        }

        else -> {
            return Color.parseColor("#1A66D2")
        }

    }
}