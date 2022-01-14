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
        requestBuilder.addHeader("assignee-code", "3845f0")
        requestBuilder.addHeader("User-Agent", "/beans/ios/13.4.1/ai.beans.isp/2.1")

        requestBuilder.url(url)
        return requestBuilder.build()
    }

}