package ai.beans.common.custom_markers

import ai.beans.common.DummyEvent
import ai.beans.common.R
import ai.beans.common.beanbusstation.BeansBusStation
import ai.beans.common.panels.PanelControlInterface
import ai.beans.common.panels.PanelInteractionListener
import ai.beans.common.pojo.IconItem
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.widgets.LockableBottomSheetBehavior
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BeansMarkerPanel  : NestedScrollView, PanelControlInterface, MarkerAdapter.ItemClickListener {
    var items : ArrayList<IconItem>?=null
    var markerContainer : RelativeLayout?= null
    var markerRV : RecyclerView?=null
    var closeBtn : ImageView?=null
    var behaviour : BottomSheetBehavior<View>?= null
    var fragment : BeansFragment ?= null
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.marker_panel, this, true)
        markerContainer = v.findViewById(R.id.markerDataContainer)
        markerRV=v.findViewById(R.id.markerRV)
        closeBtn=v.findViewById(R.id.closeBtn)
        closeBtn?.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                hide()
                //EventBus.getDefault().post(ShowCard())
            }
        })
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                behaviour = LockableBottomSheetBehavior.from(this@BeansMarkerPanel as View)
                behaviour?.setBottomSheetCallback(mBottomSheetBehaviorCallback)
                behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }

    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                bottomSheet.post { behaviour?.setState(BottomSheetBehavior.STATE_EXPANDED) }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }
    fun initMarkerList(markerData:ArrayList<IconItem>)
    {
        var filteredMarkers=ArrayList<IconItem>()
        for (item in markerData) {
            if (item.type.equals("ENTRANCE")
                || item.type.equals("SOCIETY_GATE")
                || item.type.equals("PARKING")
                || item.type.equals("ELEVATOR")
                || item.type.equals("DELIVERY_LOCKER")
                || item.type.equals("DOG")
                || item.type.equals("MAILBOX")
                || item.type.equals("ROAD_BLOCK")) {
                filteredMarkers.add(item)
            }
        }
        this.items = filteredMarkers
        markerRV?.layoutManager= LinearLayoutManager(context)
        markerRV?.adapter = MarkerAdapter(filteredMarkers, context, this)

    }

    override fun expand() {
        visibility = View.VISIBLE
        markerRV?.scrollToPosition(0)
        behaviour?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun collapse() {
    }


    override fun hide() {
        visibility = View.INVISIBLE
        behaviour?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setOwnerFragment(ownerFragment: BeansFragment) {
        fragment = ownerFragment
        var bus = BeansBusStation.BusStation.getBus(fragment!!.fragmentId!!)
        bus?.register(this)
    }

    override fun setPanelInteractionListener(listener: PanelInteractionListener) {
    }


    override fun onItemClick(position: Int) {
        BeansBusStation.getBus(fragment!!.fragmentId!!)?.post(items?.get(position)!!)
        hide()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event : DummyEvent) {
    }



}
