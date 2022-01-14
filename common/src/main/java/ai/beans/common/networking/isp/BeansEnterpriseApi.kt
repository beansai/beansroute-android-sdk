package ai.beans.common.networking.isp

import ai.beans.common.networking.Envelope
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.pojo.*
import retrofit2.http.*

interface BeansEnterpriseApi {
    @POST("consumer/v1/lists/optimizewithoutroute/items")
    suspend fun optizeStops(
        @Query("start") start: GeoPoint? = null,
        @Query("end") endLoc: GeoPoint? = null,
        @Query("moveToFront") moveToFront: Boolean? = null,
        @Body body: OptimizeStopRequest
    ): retrofit2.Response<Envelope<RouteStops>>

    @POST("consumer/v1/lists/pathwithoutroute")
    suspend fun getPathForRoute(@Body body: RouteStops): retrofit2.Response<Envelope<RoutePath>>

    @POST("consumer/v1/lists/assignee/location")
    suspend fun updateDriverLocation(@Body loc: DriverLocation): retrofit2.Response<Envelope<Void>>

    @POST("consumer/v1/lists/optimizewithunits")
    suspend fun optimizeStopsWithUnits(): retrofit2.Response<Envelope<RouteStops>>

    //Move pins
    @POST("consumer/v2/search/overrides")
    suspend fun postNewLocationForPrimaryMarker(
        @Body newLocationHashMap: HashMap<String, ArrayList<LocationUpdateRequest>>,
        @Query("query_id") query_id: String,
        @Query("list_item_id") list_item_id: String
    ): retrofit2.Response<Envelope<Void>>

}