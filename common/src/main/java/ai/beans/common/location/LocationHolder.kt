package ai.beans.common.location

import ai.beans.common.application.BeansContextContainer
import ai.beans.common.pojo.GeoPoint
import android.app.Application
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.LocationSource


class LocationHolder(application: Application) : AndroidViewModel(application), ServiceConnection,
    LocationSource {

    var currentLocation : Location ?= null
    var prevLocation : Location ?= null
    var currentLocationAsGeoPoint = GeoPoint()
    var isLocationAvailable = MutableLiveData<Boolean>()
    var mLocalBinder : BeansLocationProvider.LocalBinder ?= null
    var locationService : BeansLocationProvider ?= null
    var listener :  LocationSource.OnLocationChangedListener ?= null
    var beansListenerMap = HashMap<Int, BeansLocationListener>()

    var mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("LOCATION_HOLDER", "Broadcaster Message Rcvd")
            if (intent != null) {
                var action = intent.getAction()
                if (action != null) {
                    if (action.equals("NEW_LOCATION")) {
                        currentLocation = intent.extras?.get("LOCATION")!! as Location
                        if(currentLocation != null) {
                            currentLocationAsGeoPoint.lat = currentLocation!!.latitude
                            currentLocationAsGeoPoint.lng = currentLocation!!.longitude
                        }
                        if (isLocationAvailable.value == null) {
                            isLocationAvailable.value = true
                        }
                        listener?.onLocationChanged(currentLocation)
                        postLocationToListeners(currentLocation)
                    } else if (action.equals("SERVICE_CONNECTED")) {
                        //Lets setup the service
                        if (locationService != null) {
                            Log.d("LOCATION_HOLDER", "library alive")
                            locationService?.setup()
                        }
                    }
                }
            }
        }
    }


    init {
        Log.d("LOCATION_HOLDER", "Lib Init")
        setupBroadcastReciever()
    }

    suspend fun setupService() {
        setupServiceConnection()
    }

    fun addBeansLocationListener(listener: BeansLocationListener) {
        beansListenerMap.put(listener.hashCode(), listener)
    }

    fun removeBeansLocationListener(listener: BeansLocationListener) {
        beansListenerMap.remove(listener.hashCode())
    }

    private fun postLocationToListeners(currentLocation: Location?) {
        if(currentLocation != null) {
            for (listener in beansListenerMap.values) {
                var notify = false
                if(listener.prevLocation != null) {
                    Log.d("LOC_BEANS", "Have prev")
                    var distance = currentLocation.distanceTo(listener.prevLocation)
                    Log.d("LOC_BEANS", distance.toString())
                    if(distance > listener.thresholdInMeters) {
                        listener.prevLocation = currentLocation
                        notify = true
                    }
                } else {
                    Log.d("LOC_BEANS", "No prev")
                    listener.prevLocation = currentLocation
                    notify = true
                }
                if(notify)
                    listener.locationChanged(currentLocation)
            }
        }
    }

    private fun setupBroadcastReciever() {

        var intentFilter = IntentFilter("NEW_LOCATION")
        intentFilter.addAction("SERVICE_CONNECTED")
        intentFilter.addAction("SERVICE_DISCONNECTED")
        LocalBroadcastManager.getInstance(BeansContextContainer.application!!).registerReceiver(
            mMessageReceiver, intentFilter);

    }

    private fun setupServiceConnection() {
        Log.d("LOCATION_HOLDER", "Binding to Service")
        val mIntent = Intent()
        //mIntent.setClassName("ai.beans.stage.consumer", "ai.beans.common.location.BeansLocationProvider")//BeansApplication.mInstance?.applicationContext, BeansLocationProvider::class.java)
        BeansContextContainer.context?.let {
            mIntent.setClass(it, BeansLocationProvider::class.java)
            it.bindService(mIntent, this, BIND_AUTO_CREATE)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d("LOCATION_HOLDER", "onServiceDisconnected")
        locationService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("LOCATION_HOLDER", "onServiceConnected")
        mLocalBinder = service as BeansLocationProvider.LocalBinder
        locationService = mLocalBinder?.getLocationService()
        val intent = Intent("SERVICE_CONNECTED")
        LocalBroadcastManager.getInstance(BeansContextContainer.context!!).sendBroadcast(intent)
        mLocalBinder?.onBackground(getApplication())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        //getApplication<BeansApplication>().applicationContext.unbindService(this)
        //A special case. Here there are two scenarios we end up with
        //1. Kill switch
        //2. Finish activity
        //in 1st case activity.isFinishing is false
        //in 2nd case activity.isFinishing is true, so we use it as a trigger to ensure service is quit properly.
//        if (activity.isFinishing()) {
//            if (appForegroundService != null) {
//                appForegroundService.terminate()
//            }
//        }
    }


    override fun deactivate() {
        listener = null
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        Log.d("LOCATION_HOLDER", "map binding to location")
        this.listener = listener
    }




}