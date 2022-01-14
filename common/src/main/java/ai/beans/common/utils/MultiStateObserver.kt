package ai.beans.common.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.lang.Exception

class MultiStateObserver {

    var readyMap = HashMap<Int, Boolean>()
    var idSet = HashSet<Int>()
    var hasSetIds = false

    var multiStateIsReady = MutableLiveData<Boolean>()

    fun setStateIds(ids : ArrayList<Int>) {
        idSet.addAll(ids)
        hasSetIds = true
    }
    fun setObserverFor(lifercycleOwner: LifecycleOwner, mutableLiveData: MutableLiveData<*>, id: Int) {

        if(!hasSetIds) {
            throw(Exception("Set Ids before calling setObserver"))
        }

        if(!idSet.contains(id)) {
            throw(Exception("Please make sure Id passed in is in the set"))
        }

        readyMap[id] = false
        mutableLiveData?.observe(lifercycleOwner, Observer{
            if(it != null) {
                readyMap[id] = true
                checkIfComplete()
            }
        })
    }

    fun checkIfComplete() {
        var isDone = true
        for(id in idSet) {
            if(readyMap[id] != true ) {
                isDone = false
                break
            }
        }
        if(isDone) {
            multiStateIsReady.value = true
        }
    }

    fun reset() {
        for(id in idSet) {
            readyMap[id] = false
        }
    }


}