package ai.beans.common.networking

import ai.beans.common.application.AppInfo
import ai.beans.common.application.BeansContextContainer
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url()

        val url = setupDefaultQueryParams(originalHttpUrl).build()
        val request = setupDefaultHeaders(url, original)

        return chain.proceed(request)
    }

    protected fun setupDefaultQueryParams(originalHttpUrl: HttpUrl): HttpUrl.Builder {
        val builder = originalHttpUrl.newBuilder()
        return builder
    }

    protected fun setupDefaultHeaders(url: HttpUrl, original: Request): Request {
        val appInfo = AppInfo(BeansContextContainer.context!!)

        // Request customization: add request headers
        val requestBuilder = original.newBuilder()
        var userAgentStr = appInfo.userAgent
        requestBuilder.addHeader("User-Agent", "/sdk/ai.beans.isp")
        requestBuilder.url(url)
        return requestBuilder.build()
    }

}