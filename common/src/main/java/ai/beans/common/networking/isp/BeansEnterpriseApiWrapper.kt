package ai.beans.common.networking.isp

import ai.beans.common.networking.ApiResponse
import ai.beans.common.networking.Envelope
import ai.beans.common.pojo.*
import ai.beans.common.pojo.search.LocationUpdateRequest
import ai.beans.common.pojo.search.NoteItem
import ai.beans.common.pojo.search.NoteResponse
import ai.beans.common.pojo.search.SearchResponse
import java.io.IOException
import java.lang.reflect.UndeclaredThrowableException

suspend fun optimizeStopList(
    stops: OptimizeStopRequest,
    startLocation: GeoPoint? = null,
    endLocation: GeoPoint? = null,
    moveToFront: Boolean? = true
): Envelope<RouteStops> {
    try {
        var response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.optimizeStops(
            startLocation,
            endLocation,
            null,
            stops
        )
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (exception: IOException) {
        var env = Envelope<RouteStops>()
        return ApiResponse.handleResponse(null, env)
    }
}

suspend fun getPathForRoute(stops: RouteStops): Envelope<RoutePath> {
    try {
        var response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.getPathForRoute(stops)
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (exception: IOException) {
        var env = Envelope<RoutePath>()
        return ApiResponse.handleResponse(null, env)
    } catch (undeclaredException: UndeclaredThrowableException) {
        var env = Envelope<RoutePath>()
        return ApiResponse.handleResponse(null, env)
    }
}

suspend fun updateDriverLocation(location: DriverLocation): Envelope<Void> {
    try {
        var response =
            BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.updateDriverLocation(location)
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (exception: Exception) {
        var env = Envelope<Void>()
        return ApiResponse.handleResponse(null, env)
    } catch (undeclaredException: UndeclaredThrowableException) {
        var env = Envelope<Void>()
        return ApiResponse.handleResponse(null, env)
    }
}

suspend fun optimizeStopsWithUnits(): Envelope<RouteStops> {
    try {
        var response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.optimizeStopsWithUnits()
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (exception: IOException) {
        var env = Envelope<RouteStops>()
        return ApiResponse.handleResponse(null, env)
    }
}

suspend fun postNewLocationForPrimaryMarker(
    newLocationInfo: HashMap<String, ArrayList<LocationUpdateRequest>>,
    queryId: String,
    listItemId: String
): Envelope<Void> {
    try {
        var response =
            BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.postNewLocationForPrimaryMarker(
                newLocationInfo, queryId, listItemId
            )
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (exception: IOException) {
        var env = Envelope<Void>()
        return ApiResponse.handleResponse(null, env)
    }
}

suspend fun getSearchResponse(address: String, unit: String?, center: GeoPoint?): Envelope<SearchResponse> {

    try {
        val response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.getSearchResponse(address, unit, center)
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (ex:IOException) {
        var managedResponse = ApiResponse.handleResponse(null, Envelope<SearchResponse>())
        return managedResponse

    }
}

suspend fun getAddressNotes(queryId: String): Envelope<NoteResponse> {
    try {
        val response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.getAddressNotes(queryId)
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (ex:IOException) {
        var managedResponse = ApiResponse.handleResponse(null, Envelope<NoteResponse>())
        return managedResponse

    }
}

suspend fun postAddressNotes(queryId: String, notesMap: HashMap<String, ArrayList<NoteItem>>): Envelope<NoteResponse> {
    try {
        val response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.postAddressNotes(queryId, notesMap)
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (ex:IOException) {
        var managedResponse = ApiResponse.handleResponse(null, Envelope<NoteResponse>())
        return managedResponse

    }
}