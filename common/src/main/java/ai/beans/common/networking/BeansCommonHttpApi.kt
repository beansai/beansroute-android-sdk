package ai.beans.common.networking

import ai.beans.common.pojo.MarkerData
import retrofit2.Call
import retrofit2.http.GET

interface BeansCommonHttpApi {
    @GET("consumer/v2/search/notes/markers/info")
    suspend fun getMarkerInfo() : retrofit2.Response<Envelope<MarkerData>>
}
