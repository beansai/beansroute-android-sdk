package ai.beans.common.utils

import ai.beans.common.application.BeansApplication
import android.content.res.Configuration
import android.content.res.Configuration.*
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.greenrobot.eventbus.EventBus

object MapMarkerBitmapCache : LifecycleObserver{
    enum class ICON_MODE {NONE, DARK, LIGHT}
    var currentMode = UI_MODE_NIGHT_UNDEFINED
    var bmpLassoedLastMarker : Bitmap?= null
    var bmpLassoedMarker : Bitmap?= null
    var bitmapHashMap = HashMap<String, Bitmap>()
    var bitmapWithTextHashMap = HashMap<String, Bitmap>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun checkIfDarkMode() {
        var uiMode = BeansApplication.getContext()?.resources?.configuration?.uiMode
        var appUiSetting = AppCompatDelegate.getDefaultNightMode()
        if(uiMode != null) {
            var systemUiNightMode = uiMode and Configuration.UI_MODE_NIGHT_MASK
            if(appUiSetting == MODE_NIGHT_UNSPECIFIED || appUiSetting == MODE_NIGHT_FOLLOW_SYSTEM) {
                //The user has not set a mode in settings....just follow system
                if(currentMode != systemUiNightMode) {
                    bitmapHashMap.clear()
                }
                currentMode = systemUiNightMode
            } else {
                if(appUiSetting == MODE_NIGHT_YES && currentMode != UI_MODE_NIGHT_YES) {
                    bitmapHashMap.clear()
                    currentMode = UI_MODE_NIGHT_YES
                }

                if(appUiSetting == MODE_NIGHT_NO && currentMode != UI_MODE_NIGHT_NO) {
                    bitmapHashMap.clear()
                    currentMode = UI_MODE_NIGHT_NO
                }
            }
        }
    }

}