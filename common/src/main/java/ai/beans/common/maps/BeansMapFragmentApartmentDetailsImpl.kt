package ai.beans.common.maps

import ai.beans.common.R
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.events.CollapseCard
import ai.beans.common.events.ExpandCard
import ai.beans.common.events.HideCard
import ai.beans.common.events.ShowCard
import ai.beans.common.location.LocationHolder
import ai.beans.common.maps.renderers.StopsRendererApartmentImpl
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.widgets.stops.BeansStopPanelImpl
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BeansMapFragmentApartmentDetailsImpl : BeansFragment() {
    var addressDetailsRendererApartmentImpl : StopsRendererApartmentImpl? = null
    var address : String? = null
    var unit : String? = null
    var lat : String? = null
    var long : String? = null
    var mapFragment: BeansMapFragmentImpl? = null
    var movepinButton: RoundMapButton? = null

    lateinit var containerPanel : BeansStopPanelImpl
    var locationHolder : LocationHolder? = null
    var routeStopId: String? = null

    var customMarkerImagesViewModel : CustomMarkerImagesViewModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addressDetailsRendererApartmentImpl = StopsRendererApartmentImpl(this, savedInstanceState)
        addressDetailsRendererApartmentImpl?.plugIn()

        address = arguments?.getString("ADDRESS")
        unit = arguments?.getString("UNIT")
        lat = arguments?.getString("LATITUDE")
        long = arguments?.getString("LONGITUDE")
        routeStopId = arguments?.getString("STOP_ID")

        locationHolder = ViewModelProviders.of(activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            LocationHolder::class.java)

        customMarkerImagesViewModel = ViewModelProviders.of(activity!!).get(
            CustomMarkerImagesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_map_view_apartment, container, false)

        val fragment_map = childFragmentManager?.findFragmentById(R.id.map_view)
        if(fragment_map != null) {
            mapFragment = fragment_map as BeansMapFragmentImpl
            addressDetailsRendererApartmentImpl?.setMap(mapFragment!!)
        }

        containerPanel = v.findViewById(R.id.containerPanel)
        containerPanel.setOwnerFragment(this)
        containerPanel.setCurrentPanelVisibilityState(BottomSheetBehavior.STATE_HIDDEN)

        movepinButton= v.findViewById(R.id.movepin_button)
        movepinButton?.visibility = View.VISIBLE
        movepinButton?.mapButtonlistener = object : RoundMapButton.MapButtonListener {
            override fun buttonClicked(btnId: Int, state: Boolean) {
                addressDetailsRendererApartmentImpl?.onMarkerJumpEnd()
            }
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        addressDetailsRendererApartmentImpl?.updateViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //outState.putInt("PANEL_STATE", containerPanel.getCurrentPanelVisibilityState())
    }


    fun hideCurrentPanel() {
        containerPanel.hide()
    }

    fun showCurrentPanel() {
        containerPanel.collapse()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : CollapseCard) {
        MainScope().launch {
            hideCurrentPanel()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : ExpandCard) {
        MainScope().launch {
            showCurrentPanel()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : HideCard) {
        MainScope().launch {
            hideCurrentPanel()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : ShowCard) {
        MainScope().launch {
            showCurrentPanel()
        }
    }

    override fun setScreenName() {
        screenName = "apt_details_map_fragment"
    }

    fun getVisibleMapRect() : Rect {
        var fullMapRect = mapFragment?.getVisibleRect()
        var panelRect = containerPanel.getPanelRect()
        var visibleRect = Rect()
        visibleRect.left = fullMapRect?.left!!
        visibleRect.top = fullMapRect?.top!!
        visibleRect.right = fullMapRect?.right!!
        visibleRect.bottom = fullMapRect?.bottom - panelRect.top
        return visibleRect
    }
}