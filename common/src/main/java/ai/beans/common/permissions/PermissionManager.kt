package ai.beans.common.permissions

import android.app.Activity

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

class PermissionManager {
    var ownerFragment : Fragment? = null
    var context : Context ?= null
    var activity : Activity?= null
    var REQ_CODE : Int = 1
    var isPermissionPromptShown = false;
    var isPermissionEverRequested = false;

    constructor(fragment: Fragment) {
        ownerFragment = fragment
        context = ownerFragment?.context
        activity = ownerFragment?.activity
    }

    fun isPermissionGranted(permission:String) : Boolean {
            context?.let {
                val ret = ContextCompat.checkSelfPermission(it, permission)
                return (ret == PackageManager.PERMISSION_GRANTED)
            }
            return false
    }

    fun isPermissionShown(permission:String) : Boolean {
            return isPermissionPromptShown
    }

    fun isPermissionDeniedForever(permission: String) : Boolean {
        activity?.let {
            return (isPermissionShown(permission) && !ActivityCompat.shouldShowRequestPermissionRationale(it, permission));
        }

        return true;
    }

    fun isPermissionEverRequested(permission: String) : Boolean {
        return isPermissionEverRequested
    }

    fun requestPermission(permission: String) {
        activity?.let {
            isPermissionEverRequested = true
            ownerFragment?.requestPermissions(arrayOf(permission), REQ_CODE)
        }
    }

    fun requestPermissions(permissions: Array<String>) {
        activity?.let {
            isPermissionEverRequested = true
            ownerFragment?.requestPermissions(permissions, REQ_CODE)
        }
    }

    fun updatePermissionState(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQ_CODE) {

        }
        isPermissionPromptShown = true;
    }
}

