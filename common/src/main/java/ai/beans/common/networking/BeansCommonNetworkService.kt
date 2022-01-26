package ai.beans.common.networking

import ai.beans.common.init.LibInit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BeansCommonNetworkService {
    open var BEANS_API : BeansCommonHttpApi ?= null

    init {
        val httpClient = OkHttpClient.Builder()
        httpClient.callTimeout(300, TimeUnit.SECONDS);
        httpClient.connectTimeout(300, TimeUnit.SECONDS);
        httpClient.readTimeout(300, TimeUnit.SECONDS);
        httpClient.writeTimeout(300, TimeUnit.SECONDS);
        httpClient.addInterceptor(NetworkInterceptor())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api2.beans.ai")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()

        BEANS_API = retrofit?.create(BeansCommonHttpApi::class.java)

    }
}