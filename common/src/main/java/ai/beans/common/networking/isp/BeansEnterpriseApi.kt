package ai.beans.common.networking.isp

import ai.beans.common.networking.Envelope
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.pojo.*
import ai.beans.common.pojo.search.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface BeansEnterpriseApi {
    @POST("consumer/v1/lists/optimizewithoutroute/items")
    suspend fun optimizeStops(
        @Query("start") start: GeoPoint? = null,
        @Query("end") endLoc: GeoPoint? = null,
        @Body body: OptimizeStopRequest
    ): retrofit2.Response<Envelope<RouteStops>>

    @POST("consumer/v1/lists/pathwithoutroute")
    suspend fun getPathForRoute(@Body body: RouteStops): retrofit2.Response<Envelope<RoutePath>>

    @POST("consumer/v1/lists/assignee/location")
    suspend fun updateDriverLocation(@Body loc: DriverLocation): retrofit2.Response<Envelope<Void>>

    @POST("consumer/v1/lists/optimizewithunits")
    suspend fun optimizeStopsWithUnits(): retrofit2.Response<Envelope<RouteStops>>

    @Multipart
    @POST("image/v2/upload")
    suspend fun uploadImage(@Part image : MultipartBody.Part) : retrofit2.Response<Envelope<ImageUploadResponse>>

    //Move pins
    @POST("consumer/v2/search/overrides")
    suspend fun postNewLocationForPrimaryMarker(
        @Body newLocationHashMap: HashMap<String, ArrayList<LocationUpdateRequest>>,
        @Query("query_id") query_id: String,
        @Query("list_item_id") list_item_id: String
    ): retrofit2.Response<Envelope<Void>>

    @GET("consumer/v2/search/beans")
    suspend fun getSearchResponse(@Query("address") address : String,
                                 @Query("unit") unit : String?,
                                 @Query("origin") center : GeoPoint?,
                                 @Header("measurement-system") system : String = "IMPERIAL") : retrofit2.Response<Envelope<SearchResponse>>

    @GET("consumer/v2/search/notes")
    suspend fun getAddressNotes(@Query("query_id") query_id : String) : retrofit2.Response<Envelope<NoteResponse>>

    @POST("consumer/v2/search/notes")
    suspend fun postAddressNotes(@Query("query_id") query_id : String,
                                 @Body feedback_note_items : HashMap<String, ArrayList<NoteItem>>) : retrofit2.Response<Envelope<NoteResponse>>

}