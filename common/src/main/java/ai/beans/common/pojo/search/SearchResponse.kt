package ai.beans.common.pojo.search

import ai.beans.common.pojo.GeoPoint

class SearchResponse {
    var routes : ArrayList<Route> ?= null
    var query_id : String ?= null
    var tracking_id: String ?= null

    fun getMarkers() : ArrayList<MarkerInfo>? {
        if(routes != null && routes!![0] != null && routes!![0].route_ui_data != null) {
            return routes!![0].route_ui_data!!.markers
        } else {
            return null
        }
    }

    fun getMarkerLinks() : ArrayList<MarkerLink>? {
        if(routes != null && routes!![0] != null && routes!![0].route_ui_data != null) {
            return routes!![0].route_ui_data!!.marker_links
        } else {
            return null
        }
    }

    fun getPolygonPoints() : ArrayList<GeoPoint>? {
        if(routes != null && routes!![0] != null && routes!![0].route_ui_data != null && routes!![0].route_ui_data!!.building_shape != null) {
            return routes!![0].route_ui_data!!.building_shape!!.point
        } else {
            return null
        }
    }

    fun getNavigationPoint() : GeoPoint? {
        if(routes != null && routes!![0] != null && routes!![0].navigate_to != null ) {
            return routes!![0].navigate_to
        } else {
            return null
        }

    }
}