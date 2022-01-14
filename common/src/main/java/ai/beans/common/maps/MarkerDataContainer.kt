package ai.beans.common.maps

class MarkerDataContainer<T>(val id : String, val data : T) {
}

fun <T> createMarkerTag(id : String, data : T) : MarkerDataContainer<T> {
    return MarkerDataContainer(id, data)
}

fun getDataFromTag(tag: Any, rendererId: String): Any? {
    if(tag is MarkerDataContainer<*>) {
        if(tag.id.equals(rendererId)) {
            return tag.data
        }  else {
            return null
        }
    } else {
        return null
    }
}
