package ai.beans.common.networking.isp

import ai.beans.common.application.AppInfo
import ai.beans.common.application.BeansApplication
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

public class BeansEnterpriseNetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url()

        return chain.proceed(
            setupDefaultHeaders(
                setupDefaultQueryParams(originalHttpUrl).build(),
                original
            )
        )
    }

    protected fun setupDefaultQueryParams(originalHttpUrl: HttpUrl): HttpUrl.Builder {
        val builder = originalHttpUrl.newBuilder()
        return builder
    }

    protected fun setupDefaultHeaders(url: HttpUrl, original: Request): Request? {
        val appInfo = AppInfo(BeansApplication.getInstance()?.applicationContext!!)

        // Request customization: add request headers
        val requestBuilder = original.newBuilder()

        //firebase token
        requestBuilder.addHeader("authKey", "E8AD22C0B4118107E625F96E5A0460CACE6A9EA14F6843E7F65F08E248ABEC079F36075E4AFA657F5CE3C49EE409E9DB5D55AD8011F877EF81CA47F0E0CBA3898EDEF36A3D60C7F2A5CD28C724E8E2FC8CF973E9CF8A43ED4DEADEDA0C4A11951007C601")
        requestBuilder.addHeader("userBuid", "0SHNlOQWH6-qUibtZ_TAVlAo05cIW0XJ7l1hiINQH79hpp0KK6MgYvyQTHw22whi1H6cxB62iBg78TQEBBbW-AHC808XkC-jbFXUz71p8XFnwGy4g4HsT3VGnElDkr4yRWQW-A")
        requestBuilder.addHeader("assignee-code", "3845f0")
        requestBuilder.addHeader("User-Agent", "/beans/ios/13.4.1/ai.beans.isp/2.1")

        requestBuilder.url(url)
        return requestBuilder.build()
    }

}