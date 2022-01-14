package ai.beans.common.init

import okhttp3.Interceptor
class LibInit {
    companion object {
        var API_SERVER : String ?= null
        var networkInterceptor : Interceptor ?= null
    }
}
fun init(api_domain: String, networkInterceptor: Interceptor) {
    LibInit.API_SERVER = api_domain
    LibInit.networkInterceptor = networkInterceptor

}