package ai.beans.common.custom_markers

import ai.beans.common.networking.Envelope
import ai.beans.common.networking.getMarkerInfo
import ai.beans.common.pojo.IconItem
import ai.beans.common.pojo.MarkerData
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class CustomMarkerImagesViewModel (application: Application)  : AndroidViewModel(application) {
    var markerData : MarkerData?=null
    var isNewDataAvailable = MutableLiveData<Boolean>()
    var markerTypeMap = HashMap<String, IconItem>()
    var gson : Gson

    init {
        Log.d("MarkerViewModel", "Init")
        gson = Gson()
        fetchCustomMarkerData()
    }

    private fun fetchCustomMarkerData(): MarkerData?
    {
        var markerResponse : Envelope<MarkerData>?= null
        MainScope().launch {

            var fetchMarkers = MainScope().async(Dispatchers.IO) {
                markerResponse = getMarkerInfo()
            }
            fetchMarkers.await()
            if (markerResponse != null && markerResponse!!.success) {
                Log.d("MarkerViewModel", "Fetch OK")
                markerData = markerResponse?.data
                //Add all the icon data to a map
                //Note: we have a list of icons (with "type")
                //and a list of "types"
                //We add ONLY icons that have a "type" that is in the list of types
                //That way if the server has changed the types we support, we ignore the icons
                var tempMapOfIcons = HashMap<String?, IconItem>()
                for (iconInfo in markerData!!.icons!!) {
                    tempMapOfIcons.put(iconInfo.type, iconInfo)
                }
                for (iconType in markerData!!.types!!) {
                    if (tempMapOfIcons.containsKey(iconType)) {
                        var iconItem = tempMapOfIcons[iconType]
                        markerTypeMap.put(iconType, iconItem!!)
                    }
                }
                isNewDataAvailable.value = true
            }
        }
        return markerResponse?.data
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun getIconItemForType(type: String?) : IconItem? {
        return markerTypeMap.get(type)
    }

}