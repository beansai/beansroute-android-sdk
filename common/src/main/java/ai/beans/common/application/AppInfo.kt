package ai.beans.common.application
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo

class AppInfo {
    var context : Context ?= null
    var versionName: String? = null
    var versionCode: Int = 0
    var versionCodeString: String? = null
    var userAgent: String ?= null

    constructor(context: Context) {
        this.context = context

        // initialize package and version info
        var packageName = context.getPackageName()
        var pInfo: PackageInfo? = null
        try {

            pInfo = context.getPackageManager().getPackageInfo(packageName, 0)
            versionName = (if (pInfo != null && pInfo.versionName != null) pInfo.versionName else "")
            versionCode = pInfo?.versionCode ?: 0
            versionCodeString = Integer.toString(versionCode)
            userAgent = System.getProperty("http.agent")
            userAgent += "/beans/" + "android/" + android.os.Build.VERSION.RELEASE + "/" + packageName + "/" + versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }


}