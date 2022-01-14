package ai.beans.common.networking

import ai.beans.common.analytics.fireNetworkErrorEvent
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import retrofit2.Response

object ApiResponse {
    var gson : Gson

    init{
        gson = Gson()
    }

    fun getErrorObject(jsonStr : String?) : Envelope<ApiError>? {
        var error : Envelope<ApiError> ?= null
        if(jsonStr != null) {
            val collectionType = object : TypeToken<Envelope<ApiError>>() {}
            error = gson.fromJson<Envelope<ApiError>>(jsonStr, collectionType.type)

        }
        return error
    }

    fun <T> handleResponse(response: Response<*>?, body: Envelope<T>?): Envelope<T> {
            if(response != null) {
                if (body != null)
                    return body
                else {
                    var env = Envelope<T>()
                    var jsonStr = response.errorBody()?.string()
                    if (jsonStr != null) {
                        val collectionType = object : TypeToken<Envelope<ApiError>>() {}
                        try {
                            val errorEnv = gson.fromJson<Envelope<ApiError>>(jsonStr, collectionType.type)

                            env.success = errorEnv.success
                            env.error = errorEnv.data
                            env.error?.httpError = response.code()
                        } catch (ex : JsonSyntaxException) {
                            env.success = false
                            val generatedError = ApiError()
                            generatedError.code = "OOPS!"
                            generatedError.message = "Something went wrong"
                            env.error = generatedError
                        }
                    } else {
                        env.success = false
                        val generatedError = ApiError()
                        generatedError.httpError = response.code()
                        generatedError.code = "OOPS!"
                        generatedError.message = "Something went wrong"
                        env.error = generatedError
                    }
                    fireNetworkErrorEvent(response)
                    return env
                }
            } else {
                var env = Envelope<T>()
                env.success = false
                val generatedError = ApiError()
                generatedError.code = "OOPS!"
                generatedError.message = "Something went wrong"
                env.error = generatedError
                return env
            }
    }
}