package ai.beans.common.pojo.search

import ai.beans.common.pojo.GeoPoint

class MarkerLink {
    var start_id: Int ?= null
    var end_id: Int ?= null
    var time_in_sec: Int ?= null
    var distance_in_meter: Double ?= null
    var route_type: String ?= null
    var center: GeoPoint ?= null
    var label: MarkerLabel ?= null
}
