package ai.beans.common.location

import ai.beans.common.application.BeansContextContainer
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


class BeansLocationProvider : Service(){

    private val mBinder = LocalBinder()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocation: Location? = null
    private val TAG = BeansLocationProvider::class.java!!.getSimpleName()
    private var mServiceHandler: Handler? = null
    private var title = "Beans!!"
    private var description = "Beans is working hard!"
    private val FOREGROUND_CHANNEL_ID = "FOREGROUND_CHANNEL_ID"
    private val NOTIFICATION_ID = 1234


    inner class LocalBinder : Binder() {
        fun getLocationService() : BeansLocationProvider {
            return this@BeansLocationProvider
        }

        fun onBackground(context : Context) {
            //Show the notification
            /*val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("my_service", "My Background Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    FOREGROUND_CHANNEL_ID
                }
            val notificationIntent = Intent(context, DummyLaunchActivity::class.java)
            //notificationIntent.setAction(Intent.ACTION_MAIN);
            //notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val builder = NotificationCompat.Builder(
                context,
                channelId
            )
            builder.setColor(ContextCompat.getColor(context, R.color.colorBlue))

            val notification = builder.setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.abc_ratingbar_small_material)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
            //notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
            this@BeansLocationProvider.startForeground(NOTIFICATION_ID, notification)*/

        }

        fun terminate() {
            this@BeansLocationProvider.stopSelf()
        }

        fun setTitle(title: String) {
            this@BeansLocationProvider.title = title
        }

        fun setDescription( description: String) {
            this@BeansLocationProvider.description = description
        }
    }

    override fun onCreate() {
        Log.d("LOCATION_SERVICE", "OnCreate")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }
    }

    fun setup() {
        Log.d("LOCATION_SERVICE", "In setup")
        //All the stuff this method does needs location permission to have be given
        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        requestLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_REDELIVER_INTENT

    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        //Utils.setRequestingLocationUpdates(this, true)
        try {
            Log.d("LOCATION_SERVICE", "requestLocationUpdates")
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, mServiceHandler?.looper)
        } catch (unlikely: SecurityException) {
            //Utils.setRequestingLocationUpdates(this, false)
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mBinder.terminate()
    }

    private fun createLocationRequest() {
        Log.d("LOCATION_SERVICE", "createLocationRequest")
        mLocationRequest = LocationRequest()
        mLocationRequest?.setInterval(100)
        mLocationRequest?.setFastestInterval(100)
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")
        mLocation = location
        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent("NEW_LOCATION")
        intent.putExtra("LOCATION", location)
        LocalBroadcastManager.getInstance(BeansContextContainer.application!!).sendBroadcast(intent)

    }

    @SuppressLint("MissingPermission")
    @Throws(SecurityException::class)
    private fun getLastLocation() {
        try {
            mFusedLocationClient?.getLastLocation()?.addOnCompleteListener(object : OnCompleteListener<Location> {
                        override fun onComplete(task: Task<Location>) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult()
                                val intent = Intent("NEW_LOCATION")
                                intent.putExtra("LOCATION", mLocation)
                                LocalBroadcastManager.getInstance(BeansContextContainer.application!!).sendBroadcast(intent)

                            } else {
                                Log.w(TAG, "Failed to get location.")
                            }
                        }
                    })
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }

    }

}