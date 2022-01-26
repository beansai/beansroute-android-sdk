package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.beanbusstation.BeansBusStation
import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.events.*
import ai.beans.common.location.LocationHolder
import ai.beans.common.maps.BeansMapFragmentAddressDetailsImpl
import ai.beans.common.maps.BeansMapFragmentApartmentDetailsImpl
import ai.beans.common.panels.PanelControlInterface
import ai.beans.common.panels.PanelInteractionListener
import ai.beans.common.pojo.GeoPoint
import ai.beans.common.pojo.RouteStop
import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.search.NoteResponse
import ai.beans.common.pojo.search.SearchResponse
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.viewmodels.CurrentActiveRouteStopViewModel
import ai.beans.common.viewmodels.RouteStopsViewModel
import ai.beans.common.widgets.*
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class BeansStopPanelImpl : NestedScrollView, ActionButtonListener, PanelControlInterface, NoteButtonsListener {

    var mapCardImpl: BeansStopCardImpl? = null
    var detailsCard: RouteStopBeansDetails? = null
    var behaviour: BottomSheetBehavior<View>? = null
    var listener: PanelInteractionListener? = null
    var childStopsContainer: ConstraintLayout? = null
    var initVisibilityState = BottomSheetBehavior.STATE_COLLAPSED
    var fragment: BeansFragment? = null
    var hideMoreInfoButton: Boolean = false
    var peekHeight: Int = 800

    var prevButton: ImageButton? = null
    var nextButton: ImageButton? = null

    var showingChildStops = false

    var adapter: ChildStopAdapter? = null
    var listView: RecyclerView? = null
    var layoutManager: LinearLayoutManager? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    )

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_panel, this, true)
        mapCardImpl = v.findViewById(R.id.stop_card)
        mapCardImpl?.setActionListener(this)
        mapCardImpl?.showActionsPanel()

        detailsCard = v.findViewById(R.id.beans_stop_details)
        detailsCard?.noteButtonListener = this

        prevButton = v.findViewById(R.id.previousStop)
        nextButton = v.findViewById(R.id.nextStop)

        childStopsContainer = v.findViewById(R.id.childStopsContainer)

        prevButton?.setOnClickListener {
            fragment?.let {
                var bus = BeansBusStation.BusStation.getBus(it.fragmentId!!)
                if (bus != null) {
                    bus.post(GoToPreviousStop())
                }
            }
        }

        nextButton?.setOnClickListener {
            fragment?.let {
                var bus = BeansBusStation.BusStation.getBus(it.fragmentId!!)
                if (bus != null) {
                    bus.post(GoToNextStop())
                }
            }
        }

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                behaviour = BottomSheetBehavior.from(this@BeansStopPanelImpl as View)
                //behaviour?.peekHeight = 200
                behaviour?.state = initVisibilityState
                // peekHeight = behaviour!!.peekHeight
                behaviour?.peekHeight = peekHeight
                behaviour?.setBottomSheetCallback(sheetCallback)
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        adapter = ChildStopAdapter(context)
        adapter?.actionButtonListener = this
        listView = v.findViewById(R.id.routeListView)
        listView?.recycledViewPool?.setMaxRecycledViews(0, 10);
        listView?.setItemViewCacheSize(10);
        ViewCompat.setNestedScrollingEnabled(listView!!, false)
        adapter?.attachAdapterToRecyclerView(listView!!)
        listView?.adapter = adapter
        layoutManager = LinearLayoutManager(context);
        layoutManager?.orientation = RecyclerView.VERTICAL
        listView?.layoutManager = layoutManager
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        var bus = BeansBusStation.BusStation.getBus(fragment!!.fragmentId!!)
        //XX bus?.unregister(this)
    }

    fun updateStopStatus(stop: RouteStop, status: RouteStopStatus) {
        MainScope().launch {
            if (stop != null) {
                stop.status = status
                if (!showingChildStops) {
                    renderStopCard(stop)
                } else {
                    var routeDataViewModel = ViewModelProviders.of(fragment!!.activity!!,
                        ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
                        RouteStopsViewModel::class.java)
                    var parentStop = routeDataViewModel.getStopDetails(stop!!.parent_list_item_id!!)!!
                    renderStopCard(parentStop)
                }
                EventBus.getDefault().post(
                    UpdateStopStatus(stopId = stop!!.list_item_id!!, status = status)
                )
            }
        }
    }

    override fun onNavigateClicked(currentStop: RouteStop?) {
        if (currentStop != null) {
            val location =
                "google.navigation:q=" + currentStop!!.position!!.latitudeAsString() + "," + currentStop!!.position!!.longitudeAsString()
            val gmmIntentUri = Uri.parse(location)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(context!!.packageManager) != null) {
                fragment?.startActivity(mapIntent)
            }
        }
    }

    override fun onDeliveredClicked(currentStop: RouteStop?) {
        if (currentStop?.status!! == RouteStopStatus.FINISHED) {
            updateStopStatus(currentStop, RouteStopStatus.NEW)
        } else {
            updateStopStatus(currentStop, RouteStopStatus.FINISHED)
        }
    }

    override fun onAttemptedClicked(currentStop: RouteStop?) {
        if (currentStop?.status!! == RouteStopStatus.FAILED) {
            updateStopStatus(currentStop, RouteStopStatus.NEW)
        } else {
            updateStopStatus(currentStop, RouteStopStatus.FAILED)
        }
    }

    override fun onMoreInfoClicked(currentStop: RouteStop?) {
        var locationHolder = ViewModelProviders.of(fragment?.getMainActivity()!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
            LocationHolder::class.java)
        locationHolder?.let {
            var b = Bundle()
            b.putString("ADDRESS", currentStop!!.address)
            b.putString("UNIT", currentStop!!.unit)
            b.putString("UNIT", currentStop!!.unit)
            b.putString("STOP_ID", currentStop!!.list_item_id)
            var currLocation = locationHolder?.currentLocation
            var center : GeoPoint?= null
            if(currLocation != null) {
                center = GeoPoint()
                center.lat = currLocation?.latitude
                center.lng = currLocation?.longitude
                b.putString("LATITUDE", center.latitudeAsString())
                b.putString("LONGITUDE", center.latitudeAsString())
            }

            if(currentStop.has_apartments) {
                fragment?.getMainActivity()?.launchFragment(BeansMapFragmentApartmentDetailsImpl::class.java, b)
            } else {
                fragment?.getMainActivity()?.launchFragment(BeansMapFragmentAddressDetailsImpl::class.java, b)
            }

        }
    }

    override fun onPrevStopClicked(currentStop: RouteStop?) {
        fragment?.let {
            var bus = BeansBusStation.BusStation.getBus(it.fragmentId!!)
            if (bus != null) {
                bus.post(GoToPreviousStop())
            }
        }
    }

    override fun onNextStopClicked(currentStop: RouteStop?) {
        fragment?.let {
            var bus = BeansBusStation.BusStation.getBus(it.fragmentId!!)
            if (bus != null) {
                bus.post(GoToNextStop())
            }
        }
    }

    override fun expand() {
        visibility = View.VISIBLE
        behaviour?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun collapse() {
        visibility = View.VISIBLE
        behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun hide() {
        visibility = View.GONE
        behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setOwnerFragment(ownerFragment: BeansFragment) {
        fragment = ownerFragment
//        var bus = BeansBusStation.BusStation.getBus(fragment!!.fragmentId!!)
//        bus?.register(this)
    }

    override fun setPanelInteractionListener(listener: PanelInteractionListener) {
        this.listener = listener
    }

    fun renderStopCard(
        newStop: RouteStop,
        showConnectorLines: Boolean = false,
        showPrevNextButtons: Boolean = false
    ) {
        mapCardImpl?.makeSkinny(false, newStop)
        mapCardImpl?.renderStop(newStop)
        if (!showConnectorLines) {
            mapCardImpl?.hideConnectorLine()
        } else {
            mapCardImpl?.showConnectorLine()
        }
        if (showPrevNextButtons) {
            mapCardImpl?.showPrevNextButtons()
        } else {
            mapCardImpl?.hidePrevNextButtons()
        }
        if (hideMoreInfoButton) {
            mapCardImpl?.hideMoreInfoButton()
        }
        mapCardImpl?.visibility = View.VISIBLE

        if (newStop.children != null && newStop.children?.size!! > 0) {
            renderChildStopsList(newStop.children)
        } else {
            renderChildStopsList(ArrayList<RouteStop>())
        }
    }

    fun renderChildStopsList(childStops: ArrayList<RouteStop>?) {
        if (!childStops.isNullOrEmpty()) {
            listView?.visibility = View.VISIBLE
            showingChildStops = true
            adapter?.setData(childStops)
            adapter?.notifyDataSetChanged()
        } else {
            listView?.visibility = View.GONE
            removeChildRows()
        }
    }

    fun removeChildRows() {
        showingChildStops = false
        childStopsContainer?.removeAllViews()
        childStopsContainer?.visibility = View.GONE
        listView?.visibility = View.GONE
    }

    fun hideBeansNotes() {
        detailsCard?.visibility = View.GONE
    }


    fun renderBeansNotes(routes: SearchResponse?, notes: NoteResponse?, customMarkerImagesViewModel: CustomMarkerImagesViewModel?) {
        detailsCard?.visibility = View.VISIBLE
        detailsCard?.setPercentageTiles(null, customMarkerImagesViewModel)
        val bubbleData = routes?.routes?.get(0)?.route_ui_data?.tiles
        detailsCard?.setInfoBubbles(bubbleData, customMarkerImagesViewModel)
        detailsCard?.setNote(notes)
    }

    override fun noteWidgetClicked(event: ShowDataEntryDialog) {
        fragment?.let {
            var bus = BeansBusStation.BusStation.getBus(it.fragmentId!!)
            if(bus != null) {
                bus.post(event)
            }
        }
    }

    fun updateNotes(note: NoteResponse) {
        detailsCard?.setNote(note)
    }

    fun showPrevNextButtons() {
        //prevButton?.visibility = View.VISIBLE
        //nextButton?.visibility = View.VISIBLE
    }

    fun hidePrevNextButtons() {
        //prevButton?.visibility = View.INVISIBLE
        //nextButton?.visibility = View.INVISIBLE
    }

    fun removePrevNextButtons() {
        prevButton?.visibility = View.GONE
        nextButton?.visibility = View.GONE
    }

    fun setCurrentPanelVisibilityState(state: Int) {
        initVisibilityState = state
    }

    fun getPanelRect(): Rect {
        var rect = Rect()
        getGlobalVisibleRect(rect)
        return rect
    }

    var sheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(p0: View, p1: Float) {
        }

        override fun onStateChanged(p0: View, newState: Int) {
            if (newState != BottomSheetBehavior.STATE_SETTLING) {
                if (initVisibilityState != newState) {
                    initVisibilityState = newState
                    listener?.onPanelStateChanged(newState)
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        var routeDataViewModel = ViewModelProviders.of(fragment!!.activity!!,
                            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)).get(
                            RouteStopsViewModel::class.java)
                        routeDataViewModel.setCurrentStop(null)
                        var currentStopViewModel = ViewModelProviders.of(
                            fragment!!.activity!!,
                            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)
                        ).get(CurrentActiveRouteStopViewModel::class.java)
                        currentStopViewModel.setCurrentRouteStop(null)
                    }
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    requestLayout()
                }
            }
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : StopConfirmationDataCompleted) {
        MainScope().launch {
            //Stop confirmation data ready...
            var stop : RouteStop ?= null
            val getStopJob = MainScope().async(Dispatchers.IO) {
                stop = RouteStopsManager.getStopDetails(event.stopId)
            }

            getStopJob.await()

            if(stop != null) {
                handleStopStatusChange(stop!!, event.status)
            }
        }
    }*/
}