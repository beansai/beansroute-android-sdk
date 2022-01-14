package ai.beans.tester

import ai.beans.common.application.BeansApplication
import ai.beans.common.init.init
import ai.beans.tester.networking.EnvConfig
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class TestApplication :BeansApplication() {
    companion object {
        fun getInstance() : BeansApplication? {
            return mInstance
        }
        fun getContext() : Context? {
            if(mInstance != null) {
                return mInstance!!.applicationContext
            } else {
                return null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mFirebaseInstance = FirebaseAnalytics.getInstance(applicationContext)
    }

    override open fun fireDeferredNotification() {
    }

    override open fun sendTokenToServer() {
    }



}