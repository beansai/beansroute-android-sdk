package ai.beans.common.ui.core

import ai.beans.common.DummyEvent
import ai.beans.common.analytics.fireScreenViewEvent
import ai.beans.common.beanbusstation.BeansBusStation
import ai.beans.common.permissions.PermissionManager
import ai.beans.common.widgets.HUD
import ai.beans.common.widgets.WaitSpinner
import android.content.Context
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

abstract class BeansFragment : DialogFragment() {
    lateinit var permmisionManager : PermissionManager
    var waitSpinner : WaitSpinner?= null
    var hud : HUD?= null
    var screenName :String ?= null
    var fragmentId :  UUID ?= null
    var parentBus : EventBus ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permmisionManager = PermissionManager(this)
        setScreenName()

        Log.d("Module", "fragment coming back")
        if(savedInstanceState != null) {
            val key = savedInstanceState.getParcelable<ParcelUuid>("FRAGMENT_ID")
            Log.d("Module", "fragment coming back")
            if (key != null) {
                Log.d("Module", "Old Id found")
                fragmentId = key.uuid
            } else {
                Log.d("Module", "Id is null!!")
            }
        } else {
            Log.d("Module", "New fragment ID")
            fragmentId = UUID.randomUUID()
        }

        registerToEventBus()
    }

    private fun registerToEventBus() {
        //Create a private bus and register to the private bus
        var privateBus = BeansBusStation.addBus(fragmentId!!)
        privateBus?.register(this)

        //Register to the default bus
        EventBus.getDefault().register(this)
    }

    private fun unregisterToEventBus() {
        //Unregister from default bus
        EventBus.getDefault().unregister(this)

        //Unregister from private bus
        var privateBus = BeansBusStation.getBus(fragmentId!!)
        if(privateBus != null) {
            privateBus.unregister(this)
            //Remove bus from station
            BeansBusStation.removeBus(fragmentId!!)
        }
    }

    abstract fun setScreenName()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(parentFragment != null && parentFragment is BeansFragment) {
            Log.d("Module", "parent is alive")
            var parent = parentFragment as BeansFragment
            if(parent != null) {
                Log.d("Module", "parent is alive 2")
                parentBus = BeansBusStation.getBus(parent!!.fragmentId!!)
                parentBus?.register(this)
            } else {
                Log.d("Module", "parent is null")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Finally...unregister from parent bus
        if(parentFragment != null && parentFragment is BeansFragment) {
            var parent = parentFragment as BeansFragment
            parentBus?.unregister(this)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        getMainActivity()?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        trackScreenView()
    }

    private fun trackScreenView() {
        if(screenName != null)
            fireScreenViewEvent(getMainActivity(), screenName!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterToEventBus()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permmisionManager.updatePermissionState(requestCode, permissions, grantResults)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("Module", "saving fragment state")
        if(fragmentId != null) {
            outState.putParcelable("FRAGMENT_ID", ParcelUuid(fragmentId))
        }

    }

    open fun handleBack(): Boolean {
        return false;
    }

    fun getMainActivity(): BeansActivity? {
        return activity as BeansActivity?
    }

    fun showHUD(text : String? = null) {
        var parentActivity = getMainActivity()
        if(parentActivity != null && !parentActivity.isDestroyed) {
            if (hud == null) {
                var activity = getMainActivity()
                activity?.let {
                    hud = HUD(it)
                }
            }
            hud?.showProgressBar()
            hud?.setText(text)
            hud?.show()
        }
    }

    fun showSuccessHUD(text : String? = null) {
        var parentActivity = getMainActivity()
        if(parentActivity != null && !parentActivity.isDestroyed) {
            if (hud == null) {
                var activity = getMainActivity()
                activity?.let {
                    hud = HUD(it)
                }
            }
            hud?.setText(text)
            hud?.showSuccessMark()
            hud?.show()
        }
    }

    fun setHUDMessage(text : String? = null) {
        var parentActivity = getMainActivity()
        if(parentActivity != null && !parentActivity.isDestroyed) {
            if (hud != null && hud!!.isShowing) {
                hud?.setText(text)
            }
        }
    }

    fun hideHUD() {
        var parentActivity = getMainActivity()
        if(parentActivity != null && !parentActivity.isDestroyed) {
            if (hud != null) {
                hud?.dismiss()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : DummyEvent) {
    }
}