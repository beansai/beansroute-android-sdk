package ai.beans.common.ui.core

import ai.beans.common.R
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment


class RootFragment : BeansFragment() {

    var fragmentDataBundle: Bundle? = null
    var childFragmentClass: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            val className = arguments!!.getString("FRAGMENT_CLASS")
            if (className != null) {
                childFragmentClass = className
                fragmentDataBundle = arguments!!.getBundle("FRAGMENT_DATA")
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.root_fragment, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (childFragmentManager.backStackEntryCount == 0) {
            childFragmentClass?.let { launchChildFragment(Class.forName(it), fragmentDataBundle) }
        }
    }

    private fun launchChildFragment(fragmentClass: Class<*>, bundle: Bundle?,
                                    asDialog: Boolean = false,
                                    replace : Boolean = false) {
        if (asDialog) {
            val childFragment = fragmentClass.newInstance() as DialogFragment
            childFragment.show(childFragmentManager, "image_source_picker")
        } else {
            val childFragment = fragmentClass.newInstance() as BeansFragment
            childFragment.arguments = bundle

            val tr = childFragmentManager.beginTransaction()
            if (childFragmentManager.backStackEntryCount == 0) {
                tr.add(R.id.rootFragment, childFragment, (childFragmentManager.backStackEntryCount + 1).toString())
            } else {
                tr.replace(R.id.rootFragment, childFragment, (childFragmentManager.backStackEntryCount + 1).toString())
            }
            tr.addToBackStack(null)
            tr.commit()
        }
    }

    override fun handleBack(): Boolean {
        if (childFragmentManager.backStackEntryCount == 1) {
            return false
        } else {
            childFragmentManager.backStackEntryCount.toString()
            val childFragment = childFragmentManager.findFragmentByTag(childFragmentManager.backStackEntryCount.toString())
            if (childFragment != null && childFragment is BeansFragment) {
                if (!childFragment.handleBack()) {
                    childFragmentManager.popBackStackImmediate()
                    return true
                } else {
                    return false
                }
            } else {
                childFragmentManager.popBackStackImmediate()
                return true
            }
        }
    }

    public fun getFragmentStackCount() : Int {
        return childFragmentManager.backStackEntryCount
    }

    fun getCurrentActiveChildFragment(): BeansFragment {
        val fm = childFragmentManager
        val top = fm.backStackEntryCount
        return childFragmentManager.findFragmentByTag(Integer.toString(top)) as BeansFragment
    }


    fun launchFragment(fragmentClass: Class<*>, bundle: Bundle?, asDialog: Boolean, replace : Boolean = false) {
        launchChildFragment(fragmentClass, bundle, asDialog, replace)
    }

    override fun setScreenName() {
        screenName = "root"
    }
}