package ai.beans.common.maps.renderers

import ai.beans.common.DummyEvent
import ai.beans.common.beanbusstation.BeansBusStation
import ai.beans.common.maps.BeansMapFragmentInterface
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.maps.mapproviders.BeansMapViewListener
import ai.beans.common.maps.markers.BeansMarkerInterface
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ui.IconGenerator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

open class BeansMapRenderer(val parentFragment: BeansFragment, val savedStateBundle: Bundle?) : LifecycleObserver,
    BeansMapViewListener {

    var iconGenerator: IconGenerator? = null
    var rendererId = UUID.randomUUID().toString()
    var mapInterface : BeansMapFragmentInterface ?= null

    init {
        iconGenerator = IconGenerator(parentFragment.context)
    }

    fun setMap(mapInterface: BeansMapFragmentInterface) {
        this.mapInterface = mapInterface
        this.mapInterface?.registerMapEventListener(rendererId, this)
    }

    open fun unplug() {
        clearContent()
        parentFragment.lifecycle.removeObserver(this)
        EventBus.getDefault().unregister(this)
        var bus = BeansBusStation.BusStation.getBus(parentFragment.fragmentId!!)
        bus?.unregister(this)
    }

    open fun plugIn() {
        parentFragment.lifecycle.addObserver(this)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        //plug into the ownerFragment's bus
        var bus = BeansBusStation.BusStation.getBus(parentFragment.fragmentId!!)
        if (bus != null && !bus.isRegistered(this)) {
            bus?.register(this)
        }
    }

    open fun clearContent() {

    }

    override fun mapMoved(
        bounds: LatLngBounds?,
        oldCenter: LatLng?,
        newCenter: LatLng?,
        distanceMoved: Double?
    ) {
    }

    override fun mapReady() {
    }

    override fun onMapClicked(location: LatLng) {
    }

    override fun onMapLongClicked(location: LatLng) {
    }

    override fun onMapMarkerClicked(marker: BeansMarkerInterface) {
    }

    override fun onMarkerDragStart(marker: BeansMarkerInterface?) {
    }

    override fun onMarkerDragEnd(marker: BeansMarkerInterface?) {
    }

    override fun onMarkerDrag(marker: BeansMarkerInterface?) {
    }

    override fun onInfoWindowClicked(marker: BeansMarkerInterface) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: DummyEvent) {
    }
}