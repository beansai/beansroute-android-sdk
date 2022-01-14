package ai.beans.common.maps.boundingbox

import ai.beans.common.pojo.GeoPoint

class BeansMapBoundingBox(val left : Double,
                          val top : Double,
                          val right : Double,
                          val bottom : Double) {

    fun isLocationInBox(location: GeoPoint) : Boolean {
        var latMatch = false
        var lonMatch = false
        //FIXME there's still issues when there's multiple wrap arounds
        //FIXME there's still issues when there's multiple wrap arounds
        latMatch = if (top < bottom) {
            //either more than one world/wrapping or the bounding box is wrongish
            true
        } else {
            //normal case
            location.lat!! < top && location.lat!! > bottom
        }


        lonMatch = if (right < left) {
            //check longitude bounds with consideration for date line with wrapping
            location.lng!! <= right && location.lng!! >= left
            //lonMatch = (aLongitude >= mLonEast || aLongitude <= mLonWest);
        } else {
            location.lng!! < this.right && location.lng!! > this.left
        }

        return latMatch && lonMatch
    }

}


