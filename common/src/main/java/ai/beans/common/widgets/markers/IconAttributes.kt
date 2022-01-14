package ai.beans.common.widgets.markers

import ai.beans.common.pojo.RouteStopStatus;
import ai.beans.common.pojo.RouteStopType;

class IconAttributes {
    var number : Int ?= null
    var addressString : String ?= null
    var showSelected : Boolean = false
    var showMarkerWithTextLabel : Boolean = false
    var hasApartments : Boolean = false

    var type : RouteStopType?= null
    var status : RouteStopStatus?= null

    var sid : String ?= null
    var useSidColors = false

}