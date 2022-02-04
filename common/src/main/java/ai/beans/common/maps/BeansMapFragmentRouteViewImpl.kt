package ai.beans.common.maps

//import kotlinx.android.synthetic.main.fragment_map_view_container.*
//import kotlinx.android.synthetic.main.route_error_dialog.*
// import kotlinx.android.synthetic.main.fragment_map_view_container.*
import ai.beans.common.R
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.events.CollapseCard
import ai.beans.common.events.ExpandCard
import ai.beans.common.events.HideCard
import ai.beans.common.events.ShowCard
import ai.beans.common.location.LocationHolder
import ai.beans.common.maps.renderers.StopsRendererRouteImpl
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.pojo.RouteStop
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.viewmodels.RouteStopsViewModel
import ai.beans.common.widgets.stops.BeansStopPanelImpl
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class BeansMapFragmentRouteViewImpl : BeansFragment() {
    var stopsRendererRouteImpl: StopsRendererRouteImpl? = null
    var stopCardPanelImpl: BeansStopPanelImpl? = null
    var mapFragment : BeansMapFragmentImpl? = null
    var locationHolder : LocationHolder? = null

    var currentPanelVisibilityState = BottomSheetBehavior.STATE_HIDDEN

    var customMarkerImagesViewModel : CustomMarkerImagesViewModel?= null
    var routeDataViewModel : RouteStopsViewModel?= null

    var errorDialog: AlertDialog ?= null

    enum class  STATE { SHOW_STOPS_MODE, SHOW_SEARCH_RESULTS_MODE}
    var currentState : STATE ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stopsRendererRouteImpl = StopsRendererRouteImpl(this, savedInstanceState)
        stopsRendererRouteImpl?.plugIn()

        locationHolder = ViewModelProviders.of(activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            LocationHolder::class.java)

        routeDataViewModel = ViewModelProviders.of(activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            RouteStopsViewModel::class.java)
        
        customMarkerImagesViewModel = ViewModelProviders.of(activity!!).get(
            CustomMarkerImagesViewModel::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_map_view_route, container, false)

        val fragment_map = childFragmentManager?.findFragmentById(R.id.map_view)
        if(fragment_map != null) {
            mapFragment = fragment_map as BeansMapFragmentImpl
            stopsRendererRouteImpl?.setMap(mapFragment!!)
        }

        //Setup the two panels that slide in from below (Map and Sid filter)
        stopCardPanelImpl = v.findViewById(R.id.containerPanel)
        stopCardPanelImpl?.setOwnerFragment(this)
        if(savedInstanceState == null) {
            stopCardPanelImpl?.initVisibilityState = currentPanelVisibilityState
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopsRendererRouteImpl?.unplug()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    fun hideCurrentPanel() {
        stopsRendererRouteImpl?.currentStopViewModel?.setCurrentRouteStop(null)
        stopCardPanelImpl?.hide()
    }

    fun showCurrentPanel() {
        stopCardPanelImpl?.renderStopCard(stopsRendererRouteImpl?.currentStopViewModel?.currentRouteStop?.value!!)
        stopCardPanelImpl?.collapse()
    }

    fun hideKeyboard() {
        val imm: InputMethodManager ? =
            getMainActivity()?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager ?
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = getMainActivity()?.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
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
        screenName = "route"
    }

    fun showStopDetails(currentStop: RouteStop?) {
        var locationHolder = ViewModelProviders.of(activity!!!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            LocationHolder::class.java)
        locationHolder?.let {
            var b = Bundle()
            b.putString("ADDRESS", currentStop!!.address)
            b.putString("UNIT", currentStop!!.unit)
            b.putString("UNIT", currentStop!!.unit)
            b.putString("STOP_ID", currentStop!!.list_item_id)
            var currLocation = locationHolder?.currentLocation
            var center : GeoPoint? = null
            if(currLocation != null) {
                center = GeoPoint()
                center.lat = currLocation?.latitude
                center.lng = currLocation?.longitude
                b.putString("LATITUDE", center.latitudeAsString())
                b.putString("LONGITUDE", center.latitudeAsString())
            }

            if(currentStop.has_apartments) {
                getMainActivity()?.launchFragment(BeansMapFragmentApartmentDetailsImpl::class.java, b)
            } else {
                getMainActivity()?.launchFragment(BeansMapFragmentAddressDetailsImpl::class.java, b)
            }

        }
    }
}

