package ai.beans.common.utils

import ai.beans.common.pojo.RouteStop
import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.RouteStopType
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

fun groupStops(stops: ArrayList<RouteStop>) : ArrayList<RouteStop> {
    var parentByLatLng = HashMap<String, RouteStop>()
    var stopsByLatLng = HashMap<String, ArrayList<RouteStop>>()

    var groupedStops = ArrayList<RouteStop>()
    stops.forEach {
        Log.d("GEOCODE", it.position.toString())
        groupedStops.add(it)

        // If something already has a parent lets discard it
        if ((it.parent_list_item_id == null || it.parent_list_item_id == "") && !it.has_apartments) {
            if (!stopsByLatLng.containsKey(it.position.toString())) {
                stopsByLatLng.put(it.position.toString(), ArrayList<RouteStop>())
            }
            stopsByLatLng.get(it.position.toString())?.add(it)
        }
        // Now make a list of existing parents
        if (it.has_apartments) {
            parentByLatLng.put(it.position.toString(), it)
        }
    }

    stopsByLatLng.entries.forEach {
        if (it.value.size > 1) {
            var parentStop = RouteStop(
                UUID.randomUUID().toString(),
                "",
                "",
                it.value.get(0).address,
                "",
                it.value.get(0).formatted_address,
                RouteStopStatus.NEW,
                0L,
                0L,
                0L,
                0L,
                "",
                0L,
                "",
                RouteStopType.DROPOFF,
                "",
                "",
                "",
                0,
                0,
                0,
                it.value.get(0).position,
                it.value.get(0).position,
                it.value.get(0).address_components,
                "",
                "",
                null,
                true,
                0,
                0
            )
            parentByLatLng.put(it.value.get(0).position.toString(), parentStop)
            groupedStops.add(parentStop)
        }

        if (parentByLatLng.containsKey(it.value.get(0).position.toString())) {
            // Needs to be grouped
            var parentStop = parentByLatLng.get(it.value.get(0).position.toString())!!
            for (routeStop in it.value) {
                routeStop.parent_list_item_id = parentStop.list_item_id
                parentStop.apartment_count = (parentStop.apartment_count ?: 0) + 1
                parentStop.num_packages = (parentStop.num_packages ?: 0) + (routeStop.num_packages ?: 0)
                parentStop.total_package_count = (parentStop.total_package_count ?: 0) + (routeStop.num_packages ?: 0)
            }
        }
    }
    return groupedStops
}