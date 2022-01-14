package ai.beans.common.networking.isp

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BeansEnterpriseNetworkService {
    open var BEANS_ENTERPRISE_API: BeansEnterpriseApi? = null

    init {
        val httpClient = OkHttpClient.Builder()
        httpClient.callTimeout(300, TimeUnit.SECONDS)
        httpClient.connectTimeout(300, TimeUnit.SECONDS)
        httpClient.readTimeout(300, TimeUnit.SECONDS)
        httpClient.writeTimeout(300, TimeUnit.SECONDS)
        httpClient.addInterceptor(BeansEnterpriseNetworkInterceptor())

        val retrofit = Retrofit.Builder()
            .baseUrl("https://isp.beans.ai/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()

        BEANS_ENTERPRISE_API = retrofit?.create(BeansEnterpriseApi::class.java)

    }
}