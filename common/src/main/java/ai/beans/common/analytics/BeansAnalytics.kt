package ai.beans.common.analytics


import ai.beans.common.application.BeansApplication
import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import retrofit2.Response

val APP_REOPEN = "app_reopen"
val NETWORK_ERROR = "network_error"

fun fireEvent(eventName: String, bundle: Bundle?) {
    val fbAnalyticsInstance = BeansApplication.mFirebaseInstance
    fbAnalyticsInstance?.logEvent(eventName, bundle)
}

fun fireAppStartEvent() {
    fireEvent(FirebaseAnalytics.Event.APP_OPEN, null)
}

fun fireAppRestartEvent() {
    fireEvent(APP_REOPEN, null)
}

fun fireNetworkErrorEvent(response: Response<*>) {
    val bundle = Bundle()
    bundle.putString("request", response.raw().request().url().uri().path)
    bundle.putInt("code", response.code())

    fireEvent(NETWORK_ERROR, bundle)
}

fun fireScreenViewEvent(activity: Activity?, screenName: String) {
    if (activity != null) {
        val fbAnalyticsInstance = BeansApplication.mFirebaseInstance
        fbAnalyticsInstance?.setCurrentScreen(activity, screenName, activity.javaClass.simpleName)
    }
}

fun setUserProperty(key: String, value: String?) {
    val fbAnalyticsInstance = BeansApplication.mFirebaseInstance
    fbAnalyticsInstance?.setUserProperty(key, value)
}

fun fireButtonPressEvent() {

}