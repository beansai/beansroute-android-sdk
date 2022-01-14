package ai.beans.common.ui.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle

open class BeansActivity : AppCompatActivity() {

    var currentSelectedRootFragmentId : Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            //fireAppRestartEvent()

        } else {
            //fireAppStartEvent()
        }
    }

    override fun onBackPressed() {
        if(currentSelectedRootFragmentId != null) {
            var rootFragment = supportFragmentManager.findFragmentByTag(currentSelectedRootFragmentId.toString()) as RootFragment?
            rootFragment?.let {
                if(it.getFragmentStackCount() == 1) {
                    //Go to prev tab OR exit app...exit app for now
                    super.onBackPressed()
                } else {
                    it.handleBack()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    open fun launchFragment(kClass: Class<*>, bundle: Bundle? = null,
                            asDialog: Boolean = false,
                            replace: Boolean = false) {
        if(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            currentSelectedRootFragmentId?.let {
                //launch this fragment in current tab
                var rootFragment = supportFragmentManager.findFragmentByTag(it.toString()) as RootFragment?
                rootFragment?.let {
                    it.launchFragment(kClass, bundle, asDialog, replace)
                }
            }
        }
    }

    open fun hideBottomBar(hide : Boolean) {
    }
}