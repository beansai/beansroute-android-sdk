package ai.beans.tester.ui

import ai.beans.common.LoginEvent
import ai.beans.common.ui.core.BeansActivity
import ai.beans.common.ui.core.RootFragment
import ai.beans.tester.R
import ai.beans.tester.events.ClearMaps
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : BeansActivity() {

    var ROOT_FRAGMENT_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        /*if(!UserSession.isLoggedIn()) {
            launchLoginFragment()
        }
        else
        {
            launchRootFragment()
        }*/
        launchRootFragment()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : LoginEvent) {

        MainScope().launch {
            var loginFragment = supportFragmentManager.findFragmentByTag("login")
            if(loginFragment != null) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.remove(loginFragment)
                transaction.commit()
            }
            launchRootFragment()
        }
    }
    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun launchRootFragment() {
        var container = findViewById<RelativeLayout>(R.id.rootContainer)
        val transaction = supportFragmentManager.beginTransaction()
        val b = Bundle()
        b.putString("FRAGMENT_CLASS", TestFragment::class.qualifiedName)
        var rootFragment = Fragment.instantiate(this, RootFragment::class.java!!.getName(), b) as RootFragment
        //tabInfo?.rootFragment = rootFragment
        transaction.add(R.id.rootContainer, rootFragment, ROOT_FRAGMENT_ID.toString())
        transaction.commit()
        currentSelectedRootFragmentId = ROOT_FRAGMENT_ID
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : ClearMaps) {
        MainScope().launch {
        }
    }



}
