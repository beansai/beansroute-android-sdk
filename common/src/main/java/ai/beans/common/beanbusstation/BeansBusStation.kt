package ai.beans.common.beanbusstation

import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.HashMap

class BeansBusStation {
    companion object BusStation {
        var busMap = HashMap<UUID, EventBus>()

        fun getBus(fragmentId: UUID) : EventBus? {
            return busMap[fragmentId]
        }

        fun addBus(fragmentId: UUID) : EventBus? {
            var bus = EventBus()
            busMap[fragmentId] = bus
            return bus
        }

        fun removeBus(fragmentId: UUID) {
            busMap.remove(fragmentId)
        }
    }
}