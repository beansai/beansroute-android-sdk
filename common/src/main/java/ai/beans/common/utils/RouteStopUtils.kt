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
    var bestIxPosition = HashMap<String, Number>()

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

            if (!bestIxPosition.containsKey(it.position.toString())) {
                bestIxPosition.put(it.position.toString(), groupedStops.size - 1)
            }
        }
        // Now make a list of existing parents
        if (it.has_apartments) {
            parentByLatLng.put(it.position.toString(), it)
        }
    }

    stopsByLatLng.entries.forEach {
        if (it.value.size > 1 || parentByLatLng.containsKey(it.value.get(0).position.toString())) {
            for (routeStop in it.value) {
                if (!parentByLatLng.containsKey(routeStop.position.toString())) {
                    var parentStop = RouteStop(
                        UUID.randomUUID().toString(),
                        "",
                        "",
                        routeStop.address,
                        "",
                        routeStop.formatted_address,
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
                        routeStop.position,
                        routeStop.position,
                        routeStop.address_components,
                        "",
                        "",
                        null,
                        true,
                        0,
                        0
                    )
                    parentByLatLng.put(routeStop.position.toString(), parentStop)
                    groupedStops.add(
                        (bestIxPosition.get(routeStop.position.toString()) ?: 0).toInt(), parentStop
                    )
                }

                // Needs to be grouped
                var parentStop = parentByLatLng.get(routeStop.position.toString())!!
                routeStop.parent_list_item_id = parentStop.list_item_id
                parentStop.apartment_count = (parentStop.apartment_count ?: 0) + 1
                parentStop.num_packages =
                    (parentStop.num_packages ?: 0) + (routeStop.num_packages ?: 0)
                parentStop.total_package_count =
                    (parentStop.total_package_count ?: 0) + (routeStop.num_packages ?: 0)
            }
        }
    }
    return groupedStops
}