package ai.beans.tester

import android.app.Application
import android.content.Context

class TestApplication : Application() {
    companion object {
        var mInstance: Application? = null

        fun getInstance() : Application? {
            return mInstance
        }
        fun getContext() : Context? {
            if (mInstance != null) {
                return mInstance!!.applicationContext
            } else {
                return null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        mInstance = this
    }
}