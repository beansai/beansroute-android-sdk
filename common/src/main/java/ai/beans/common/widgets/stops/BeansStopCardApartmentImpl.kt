package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.maps.android.ui.IconGenerator

class BeansStopCardApartmentImpl : ConstraintLayout {

    var pnlAddressApartmentImpl : BeansStopPnlAddressApartmentImpl ?= null
    var actionsPanel : ConstraintLayout ?= null
    var pnlPackageDetailsApartmentImpl : BeansStopCardPnlPackageInfoApartmentImpl ?= null
    var iconGenerator : IconGenerator?= null
    var marker_container : View ?= null
    var btnDeliveredImpl: BeansStopBtnDeliveredImpl ?= null
    var btnAttemptedImpl: BeansStopBtnAttemptedImpl ?= null
    var actionButtonListener : ActionButtonListener ?= null
    var connectorLine: View ?= null
    var currentStop: RouteStop? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        iconGenerator = IconGenerator(context)
        iconGenerator?.setBackground(ColorDrawable(Color.TRANSPARENT))

        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_card_apartment, this, true) as ConstraintLayout
        pnlAddressApartmentImpl = v.findViewById(R.id.address_panel)
        actionsPanel = v.findViewById(R.id.action_buttons_container)
        pnlPackageDetailsApartmentImpl = v.findViewById(R.id.package_info_panel)
        marker_container = v.findViewById(R.id.left_vert_line)

        btnDeliveredImpl = v.findViewById(R.id.deliveredButtonV2)
        btnDeliveredImpl?.isClickable = true
        btnAttemptedImpl = v.findViewById(R.id.attemptedButtonV2)
        btnAttemptedImpl?.visibility = GONE

        btnDeliveredImpl?.setOnClickListener{
            if(actionButtonListener != null) {
                actionButtonListener!!.onDeliveredClicked(currentStop)
            }
        }

        btnAttemptedImpl?.setOnClickListener{
            if(actionButtonListener != null) {
                actionButtonListener!!.onAttemptedClicked(currentStop)
            }
        }

        connectorLine = v.findViewById(R.id.left_connector_line)

    }

    fun renderStop(currentStop: RouteStop?) {
        //renderMarker(currentStop)
        this.currentStop = currentStop
        pnlAddressApartmentImpl?.render(currentStop)
        //actionsPanel?.render(currentStop)
        pnlPackageDetailsApartmentImpl?.render(currentStop)

        if(currentStop != null) {
            when(currentStop!!.status) {
                RouteStopStatus.FINISHED -> {
                    btnDeliveredImpl?.setSelectedState(btnAttemptedImpl?.visibility == GONE, true, false)
                    btnAttemptedImpl?.setSelectedState(btnAttemptedImpl?.visibility == GONE, true, false)
                }

                RouteStopStatus.NEW -> {
                    btnDeliveredImpl?.setSelectedState(btnAttemptedImpl?.visibility == GONE, false, false)
                    btnAttemptedImpl?.setSelectedState(btnAttemptedImpl?.visibility == GONE, false, false)
                }

                RouteStopStatus.FAILED -> {
                    btnDeliveredImpl?.setSelectedState(btnAttemptedImpl?.visibility == GONE, false, true)
                    btnAttemptedImpl?.setSelectedState(btnAttemptedImpl?.visibility == GONE, false, true)
                }
            }
        }
    }

    private fun renderMarker(routeStop: RouteStop?) {
        routeStop?.let {
            var marker = BeansStopMarkerIconImpl(context)
            var bitmap = marker.setupIcon(  iconGenerator!!,
                                            routeStop.type,
                                            routeStop.status,
                            false,
                                            routeStop.has_apartments,
                                true,
                                null)
            var bmpDrawable = BitmapDrawable(resources, bitmap)
            marker_container?.background = bmpDrawable
        }
    }


    fun hideActionsPanel() {
        actionsPanel?.visibility = View.GONE
    }

    fun showActionsPanel() {
        actionsPanel?.visibility = View.VISIBLE
    }

    fun hidePackageDetailsPanel() {
        pnlPackageDetailsApartmentImpl?.visibility = View.GONE
    }

    fun showPackageDetailsPanel() {
        pnlPackageDetailsApartmentImpl?.visibility = View.VISIBLE
    }

    fun makeSkinny( skinny : Boolean, currentStop: RouteStop?) {
        if(skinny) {
            //addressPanel?.hideButtons(currentStop)
            //actionsPanel?.hide()
        } else {
            //addressPanel?.showButtons(currentStop)
            //actionsPanel?.show()

        }
    }

    fun showConnectorLine() {
        connectorLine?.visibility = View.VISIBLE
    }

    fun hideConnectorLine() {
        connectorLine?.visibility = View.GONE
    }

    fun setActionListener(listener: ActionButtonListener) {
        actionButtonListener = listener
    }
}