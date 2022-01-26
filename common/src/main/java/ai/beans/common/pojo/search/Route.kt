package ai.beans.common.pojo.search

import ai.beans.common.pojo.GeoPoint

class Route {
    //var fragments : ArrayList<RouteFragment> ?= null
    var origin : GeoPoint ?= null
    var route_ui_data : RouteUiData ?= null
    var destination : Destination ?= null
    var navigate_to : GeoPoint ?= null
}