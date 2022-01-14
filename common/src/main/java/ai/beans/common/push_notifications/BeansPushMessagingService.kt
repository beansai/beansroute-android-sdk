package ai.beans.common.push_notifications

import ai.beans.common.application.BeansApplication
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.hypertrack.sdk.HyperTrackMessagingService

open class BeansPushMessagingService : HyperTrackMessagingService()  {



    companion object {
        private var editor: SharedPreferences.Editor
        private var savedPushTokenPrefs: SharedPreferences = BeansApplication.getContext()!!.getSharedPreferences(
            "savedPushToken", Context.MODE_PRIVATE
        )

        init {
            Log.d("BEANS_PUSH", "Push Services init")
            editor = savedPushTokenPrefs.edit()
        }

        fun saveToken(token: String) {
            editor.putString("BEAN_PUSH_TOKEN", token)?.commit()
            editor.putBoolean("BEAN_PUSH_TOKEN_SENT_TO_SERVER", false)?.commit()
        }

        fun getToken() : String? {
            return savedPushTokenPrefs.getString("BEAN_PUSH_TOKEN", null)
        }

        fun getTokenSavedFlag() : Boolean {
            return savedPushTokenPrefs.getBoolean("BEAN_PUSH_TOKEN_SENT_TO_SERVER", false)
        }

        fun setTokenSavedFlag(flag : Boolean) {
            editor.putBoolean("BEAN_PUSH_TOKEN_SENT_TO_SERVER", flag)?.commit()
        }

        fun setNotificationData(data: String) {
            editor.putString("BEANS_DATA", data)?.commit()
        }

        fun getNotificationData() : String? {
            return savedPushTokenPrefs.getString("BEANS_DATA", null)
        }

        fun clearNotificationData() {
            editor.putString("BEANS_DATA", null)?.commit()
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("BEANS_PUSH", "Message rcvd")
        var data : String ?= null
        if(remoteMessage.data != null) {
            for (key in remoteMessage.data.keys) {
                Log.d("BEANS_PUSH", key)
                Log.d("BEANS_PUSH", remoteMessage.data[key].orEmpty())
                if (key.equals("beans", true)) {
                    //We have beans data...
                    Log.d("BEANS_PUSH", "We have Beans Data")
                    data = remoteMessage.data[key]
                }
            }
        }
        if(remoteMessage.notification != null) {
            //We need to show this notification
            Log.d("BEANS_PUSH", "Has title and desc")
            Log.d("BEANS_PUSH", "title = " + remoteMessage.notification!!.title)
            Log.d("BEANS_PUSH", "body = " + remoteMessage.notification!!.body)
            var title = ""
            if(!remoteMessage.notification!!.title.isNullOrEmpty()) {
                title = remoteMessage.notification!!.title!!
            }
            var desc = ""
            if(!remoteMessage.notification!!.body.isNullOrEmpty()) {
                desc = remoteMessage.notification!!.body!!
            }
            sendNotification(title, desc, data)
        } else if(data != null) {
            Log.d("BEANS_PUSH", "Silent Push")
            if(BeansApplication.mInstance != null) {
                Log.d("BEANS_PUSH", "App alive")
                if(BeansApplication.mInstance?.isAppInForeground!!) {
                    Log.d("BEANS_PUSH", "App running")
                    postPushIntent(data)
                } else {
                    Log.d("BEANS_PUSH", "App dead")
                    setNotificationData(data)
                }
            }
        }
    }

    open fun postPushIntent(data: String) {
        //ToDo: Implement in sub class
    }


    open fun sendNotification(title: String, content: String, data : String ?) {
        //TODO: Implement in sub class
    }

    open override fun onNewToken(token: String) {
        //save the token
        Log.d("BEANS_PUSH", "onNewToken")
        Log.d("BEANS_PUSH", token)
        saveToken(token)
    }

}