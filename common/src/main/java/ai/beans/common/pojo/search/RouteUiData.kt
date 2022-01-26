package ai.beans.common.pojo.search

import ai.beans.common.pojo.GeoPoint

class RouteUiData {

    fun isUnitInMarker(unit: String?) : Boolean {
        var returnValue = false
        if(unit != null && markers != null) {
            for(marker in markers!!) {
                if(marker.type == MapMarkerType.UNIT && marker.text.equals(unit)) {
                    returnValue = true
                }
            }
        }
        return returnValue
    }

    var building_shape : BuildingShape ?= null
    var society: String ?= null
    var address: String ?= null
    var unit: String ?= null
    var markers: ArrayList<MarkerInfo> ?= null
    var marker_links: ArrayList<MarkerLink> ?= null
    var tiles: ArrayList<TileInfo> ?= null
    var percentage_tiles: ArrayList<PercentageTileInfo> ?= null
    var crime_data : CrimeData ?= null
    var tip_data : TipData ?= null
    var navigate_to: GeoPoint ?= null
    var distance : Distance ?= null
    var link: String ?= null
    var link_text: String ?= null
    var is_beans : Boolean = false
    var number_of_units : Int = 0
    var unit_info : UnitInfo ?= null

}