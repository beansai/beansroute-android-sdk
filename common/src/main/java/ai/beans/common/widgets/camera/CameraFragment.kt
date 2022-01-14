package ai.beans.common.widgets.camera

import ai.beans.common.PictureCaptured
import ai.beans.common.R
import ai.beans.common.ShowShareButton
import ai.beans.common.maps.RoundMapButton
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.utils.ObservableData
import ai.beans.common.widgets.AutofitTextureView
import android.Manifest
import android.graphics.SurfaceTexture
import android.hardware.Camera
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


var CAMERA_PHOTO : Int = 1

class CameraFragment : BeansFragment() , TextureView.SurfaceTextureListener {

    private var cameraHelper = CameraHelper()
    private var surfaceView  : AutofitTextureView?= null
    private var pictureButton : ImageView?= null
    private var flashButton: RoundMapButton? = null
    private var backButton: RoundMapButton? = null

    private var imageResultsViewModel : CameraCaptureResults?= null
    private var image_id : Int ?= null



    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        override fun handleMessage(inputMessage: Message) {
            // Gets the image task from the incoming Message object.
            //val photoTask = inputMessage.obj as PhotoTask
            if(inputMessage.what == CAMERA_PHOTO) {
                var bundle = inputMessage.data
                var captureResults = ImageCaptureResult()
                captureResults.imageFile = bundle.getString("FILE")
                captureResults.imageTypeId = image_id
                imageResultsViewModel?.imageCaptureResult?.value = ObservableData(captureResults)
                Log.d("tag", bundle.getString("FILE").orEmpty())
                getMainActivity()?.onBackPressed()
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: PictureCaptured) {
        var captureResults = ImageCaptureResult()
        captureResults.imageFile = event.file
        captureResults.imageTypeId = image_id
        imageResultsViewModel?.imageCaptureResult?.value = ObservableData(captureResults)
        Log.d("tag", event.file)
        getMainActivity()?.onBackPressed()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get the image type id that we will return
        if(savedInstanceState != null) {
            image_id = savedInstanceState.getInt("Image_id")
        } else {
            image_id = arguments?.getInt("Image_id")
        }
        imageResultsViewModel = ViewModelProviders.of(activity!!).get(CameraCaptureResults::class.java)
        val date = Date()
        cameraHelper.init(this, date.toString() + "_" + "BeansMapperImage.jpg")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.camera_view, container, false)
        setupViews(v)

        return v
    }

    override fun onResume() {
        super.onResume()
        setupOverlay(view)
    }

    override fun onPause() {
        super.onPause()
        cameraHelper?.closeCamera()
    }

    override fun hideBottomBar() : Boolean {
        return true;
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(image_id != null) {
            outState.putInt("Image_id", image_id!!)
        }
    }

    private fun setupViews(v: View?) {
        surfaceView = v?.findViewById<AutofitTextureView>(R.id.camera_surface)
        pictureButton = v?.findViewById(R.id.takePicture)
        pictureButton?.setOnClickListener {
            cameraHelper?.takePicture()
        }

        backButton = v?.findViewById(R.id.back_button)
        backButton?.setOnClickListener {
            getMainActivity()?.onBackPressed()
        }

        //Scan button
        flashButton = v?.findViewById(R.id.flash_button)
        flashButton?.mapButtonlistener = object : RoundMapButton.MapButtonListener {
            override fun buttonClicked(btnId: Int, state: Boolean) {
                flashButton?.toggleButton()
                cameraHelper?.setFlashState(!state)
            }
        }


    }

    private fun setupOverlay(v : View?) {
        if (permmisionManager.isPermissionGranted(Manifest.permission.CAMERA)) {
            surfaceView?.let {
                if(it.isAvailable) {
                    cameraHelper.openCamera(it)
                } else {
                    it.surfaceTextureListener = this
                }
            }
        } else {
            if(!permmisionManager.isPermissionEverRequested(Manifest.permission.CAMERA)) {
                if (!permmisionManager.isPermissionGranted(Manifest.permission.CAMERA)) {
                    permmisionManager.requestPermission(Manifest.permission.CAMERA)
                }
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        cameraHelper.openCamera(surfaceView)
        //Check if we should show flash button
        if(cameraHelper.mFlashSupported) {
            flashButton?.visibility = View.VISIBLE
        } else {
            flashButton?.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    fun getHandler() : Handler {
        return handler
    }

    override fun setTitle() {
    }

    override fun setScreenName() {
        screenName = "camera"
    }

}