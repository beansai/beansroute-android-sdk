package ai.beans.common.application

import ai.beans.common.ui.core.BeansActivity
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.greenrobot.eventbus.EventBus


open class BeansApplication : Application(), LifecycleObserver,
    Application.ActivityLifecycleCallbacks {

    var isAppInForeground = false
    var currentActivity: BeansActivity? = null
    var activityCount = 0


    companion object Instance {
        var TAG = BeansApplication::class.java.simpleName
        var mFirebaseInstance: FirebaseAnalytics? = null
        var mInstance: BeansApplication? = null

        fun getInstance(): BeansApplication? {
            return mInstance
        }

        fun getFirebaseInstance(): FirebaseAnalytics? {
            return mFirebaseInstance
        }

        fun getContext(): Context? {
            if (mInstance != null) {
                return mInstance!!.applicationContext
            } else {
                return null
            }
        }

    }

    override fun onTerminate() {
        super.onTerminate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onForegroundStart() {
        Log.d("BEANS_PUSH", "onForegroundStart")
        isAppInForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onForegroundStop() {
        Log.d("BEANS_PUSH", "onForegroundStop")
        isAppInForeground = false
    }

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        registerActivityLifecycleCallbacks(this)

        mInstance = this
        mFirebaseInstance = FirebaseAnalytics.getInstance(applicationContext)
        mFirebaseInstance?.setAnalyticsCollectionEnabled(true)
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d("BEANS_PUSH", "onActivityPaused" + " : " + activity?.localClassName)
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("BEANS_PUSH", "onActivityResumed" + " : " + activity?.localClassName)
    }


    override fun onActivityStarted(activity: Activity) {
        Log.d("BEANS_PUSH", "onActivityStarted" + " : " + activity?.localClassName)
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("BEANS_PUSH", "onActivityDestroyed" + " : " + activity?.localClassName)
        activityCount--
        if (currentActivity != null) {
            Log.d("BEANS_PUSH", "Destroy??")
            if (currentActivity!!.javaClass == activity?.javaClass) {
                //The activity being destoyed is the one we have...zapp it
                currentActivity = null
            }
        }

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d("BEANS_PUSH", "onActivitySaveInstanceState" + " : " + activity?.localClassName)
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("BEANS_PUSH", "onActivityStopped" + " : " + activity?.localClassName)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("BEANS_PUSH", "onActivityCreated" + " : " + activity?.localClassName)
        activityCount++
        if (activity != null && activity is BeansActivity) {
            currentActivity = activity
            fireDeferredNotification()
            sendTokenToServer()
        }
    }

    open fun fireDeferredNotification() {
        TODO("implemented in Derived class")
    }

    open fun sendTokenToServer() {
        TODO("implemented in Derived class")
    }


}