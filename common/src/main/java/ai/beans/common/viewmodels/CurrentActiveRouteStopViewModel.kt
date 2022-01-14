package ai.beans.common.viewmodels

import ai.beans.common.pojo.RouteStop
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CurrentActiveRouteStopViewModel(application: Application) : AndroidViewModel(application) {
    var currentRouteStop = MutableLiveData<RouteStop>()

    fun setCurrentRouteStop(stop : RouteStop?){
        currentRouteStop.value = stop
    }
}