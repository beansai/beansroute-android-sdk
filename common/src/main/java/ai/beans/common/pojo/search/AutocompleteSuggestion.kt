package ai.beans.common.pojo.search

import ai.beans.common.pojo.GeoPoint

class AutocompleteSuggestion {
    var address : String ?= null
    var found: Boolean = false
    var society: String ?= null
    var place_id: String ?= null
    var unit_count: Int = 0
    var location: GeoPoint ?= null
    var distance: Distance ?= null

    constructor(suggestion: AutocompleteSuggestion) {
        address = suggestion.address
        found = suggestion.found
        society = suggestion.society
        place_id = suggestion.place_id
        unit_count = suggestion.unit_count
        distance = suggestion.distance
        location = GeoPoint()

        if(suggestion.location != null) {
            location!!.lat = suggestion.location!!.lat
            location!!.lng = suggestion.location!!.lng
        }
    }

}