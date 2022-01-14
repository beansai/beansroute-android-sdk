package ai.beans.common.networking

import ai.beans.common.pojo.MarkerData
import java.io.IOException

suspend fun  getMarkerInfo(): Envelope<MarkerData> {
    try {
        val response = BeansCommonNetworkService.BEANS_API!!.getMarkerInfo()
        if (response != null && response.code() == 200) {
            return response.body()!!
        } else {
            var managedResponse = ApiResponse.handleResponse(response, response.body())
            return managedResponse
        }
    } catch (ex: IOException) {
        var managedResponse = ApiResponse.handleResponse(null, Envelope<MarkerData>())
        return managedResponse
    }
}
