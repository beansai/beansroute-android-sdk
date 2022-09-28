package ai.beans.common.widgets.stops

import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.R
import ai.beans.common.events.ShowMoreInfo
import ai.beans.common.events.ShowNextStop
import ai.beans.common.events.ShowPrevStop
import ai.beans.common.pojo.RouteStop
import ai.beans.common.widgets.markers.IconAttributes
import ai.beans.common.widgets.markers.MarkerIconHelper
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.maps.android.ui.IconGenerator
import org.greenrobot.eventbus.EventBus

//import sun.jvm.hotspot.utilities.IntArray


open class BeansStopCardImpl : RelativeLayout, LifecycleObserver {

    var pnlAddress: BeansStopPnlAddressImpl? = null
    var beansStopPnlTimeWindowImpl: BeansStopPnlTimeWindowImpl? = null
    var pnlActionsImpl: BeansStopPnlActionsImpl? = null
    var packageDetailsImpl: BeansStopPnlPackageInfoImpl? = null
    var localBus: EventBus? = null
    var currentStop: RouteStop? = null
    var ownerFragment: BeansFragment? = null
    var actionButtonListener: ActionButtonListener? = null
    var iconGenerator: IconGenerator? = null
    var marker_container: View? = null
    var infoButton: ImageButton ?= null
    var navigateButton: ImageButton? = null
    var removeTransferButtonII: Button? = null
    var connectorLine: View? = null
    var showMarkersWithSidColor = false

    var pnlNotesImpl: BeansStopPnlNotesImpl? = null
    var beansStopPnlCustomerImpl: BeansStopPnlCustomerInfoImpl? = null


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        iconGenerator = IconGenerator(context)
        iconGenerator?.setBackground(ColorDrawable(Color.TRANSPARENT))

        val v = LayoutInflater.from(context).inflate(
            R.layout.beans_stop_card,
            this,
            true
        ) as RelativeLayout

        pnlAddress = v.findViewById(R.id.address_panel)
        beansStopPnlTimeWindowImpl = v.findViewById(R.id.time_window_panel)
        pnlActionsImpl = v.findViewById(R.id.actions_panel)
        packageDetailsImpl = v.findViewById(R.id.package_info_panel)
        infoButton = v.findViewById(R.id.moreInfoBtn)
        navigateButton = v.findViewById(R.id.navigateBtn)
        pnlNotesImpl = v.findViewById(R.id.notes_panel)
        beansStopPnlCustomerImpl = v.findViewById(R.id.user_panel)

        removeTransferButtonII = v.findViewById(R.id.deleteTransferStopII)

        navigateButton?.setOnClickListener {
            if (actionButtonListener != null) {
                actionButtonListener!!.onNavigateClicked(currentStop)
            }
        }

        pnlActionsImpl?.navigateButton!!.setOnClickListener {
            if (actionButtonListener != null) {
                actionButtonListener!!.onNavigateClicked(currentStop)
            }
        }

        pnlActionsImpl?.btnDeliveredImpl!!.setOnClickListener {
            if (actionButtonListener != null) {
                actionButtonListener!!.onDeliveredClicked(currentStop)
            }
        }

        pnlActionsImpl?.btnAttemptedImpl!!.setOnClickListener {
            if (actionButtonListener != null) {
                actionButtonListener!!.onAttemptedClicked(currentStop)
            }
        }

        pnlActionsImpl?.infoButton!!.setOnClickListener{
            if(actionButtonListener != null) {
                actionButtonListener!!.onMoreInfoClicked(currentStop)
            } else {
                localBus?.post(ShowMoreInfo())
            }
        }

        pnlActionsImpl?.prevStopButton!!.setOnClickListener {
            if (actionButtonListener != null) {
                actionButtonListener!!.onPrevStopClicked(currentStop)
            } else {
                localBus?.post(ShowPrevStop())
            }
        }

        pnlActionsImpl?.nextStopButton!!.setOnClickListener {
            if (actionButtonListener != null) {
                actionButtonListener!!.onNextStopClicked(currentStop)
            } else {
                localBus?.post(ShowNextStop())
            }
        }

        infoButton?.setOnClickListener {
            if(actionButtonListener != null) {
                actionButtonListener!!.onMoreInfoClicked(currentStop)
            } else {
                localBus?.post(ShowMoreInfo())
            }
        }

        connectorLine = v.findViewById(R.id.connectorLine)
        marker_container = v.findViewById(R.id.marker)

        showMarkersWithSidColor = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun setListeners() {
        //mapFragment.containerPanel?.setPanelInteractionListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun removeListeners() {
        //mapFragment.containerPanel?.setPanelInteractionListener(this)
    }

    fun setupCard(ownerFragment: BeansFragment) {
        this.ownerFragment = ownerFragment
        this.ownerFragment!!.lifecycle.addObserver(this)
    }

    open fun renderStop(currentStop: RouteStop?) {
        renderMarker(currentStop)
        this.currentStop = currentStop

        pnlAddress?.render(currentStop)
        pnlActionsImpl?.render(currentStop)
        packageDetailsImpl?.render(currentStop)
        pnlNotesImpl?.render(currentStop)
        beansStopPnlCustomerImpl?.render(currentStop)
        beansStopPnlTimeWindowImpl?.render(currentStop)
    }

    protected fun renderMarker(routeStop: RouteStop?) {
        routeStop?.let {
            var attributes = IconAttributes()
            attributes.status = routeStop.status
            attributes.showMarkerWithTextLabel = false
            attributes.type = routeStop.type
            attributes.hasApartments = routeStop.has_apartments
            attributes.showSelected = false
            attributes.number = routeStop.route_display_number
            attributes.sid = routeStop.tracking_id
            attributes.useSidColors = showMarkersWithSidColor

            var bitmap =
                MarkerIconHelper().getMarkerIconBitmap(context, iconGenerator!!, attributes)

            var bmpDrawable = BitmapDrawable(resources, bitmap)
            marker_container?.background = bmpDrawable
        }
    }

    open fun hideActionsPanel() {
        pnlActionsImpl?.visibility = View.GONE
        pnlActionsImpl?.hide()
    }

    open fun showActionsPanel() {
        pnlActionsImpl?.visibility = View.VISIBLE
        pnlActionsImpl?.show()
    }

    fun hideMoreInfoButton() {
        //We hide the button in the actions panel as well as the outer container
        infoButton?.visibility = GONE
        pnlActionsImpl?.infoButton?.visibility = GONE
    }

    open fun makeSkinny(skinny: Boolean, currentStop: RouteStop?) {
        var innerButtonContainer = findViewById<ConstraintLayout>(R.id.general_buttons_container)
        if (skinny) {
            innerButtonContainer?.visibility = View.GONE
            connectorLine?.visibility = View.GONE
            //addressPanel?.hideParentStopButtons(currentStop)
            hideActionsPanel()
        } else {
            if (currentStop!!.has_apartments) {
                //innerButtonContainer?.visibility = View.VISIBLE
                connectorLine?.visibility = View.VISIBLE
                showActionsPanel()
            } else {
                innerButtonContainer?.visibility = View.GONE
                connectorLine?.visibility = View.GONE
                showActionsPanel()
            }
        }
    }

    fun hideConnectorLine() {
        connectorLine?.visibility = View.GONE
    }

    fun showConnectorLine() {
        connectorLine?.visibility = View.VISIBLE
    }

    fun setActionListener(listener: ActionButtonListener) {
        actionButtonListener = listener
    }

    fun showPrevNextButtons() {
        pnlActionsImpl?.showPrevNextButtons()
    }

    fun hidePrevNextButtons() {
        pnlActionsImpl?.hidePrevNextButtons()
    }
}