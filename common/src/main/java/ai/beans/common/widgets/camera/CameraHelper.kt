package ai.beans.common.widgets.camera

import ai.beans.common.PictureCaptured
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.widgets.AutofitTextureView
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class CameraHelper{

    private var mCamera: CameraDevice?= null
    private var mSurfaceView  : AutofitTextureView?= null
    private var mPreviewSize: Size? = null
    private var surfaceTexture: SurfaceTexture?= null
    private var ownerFragment : BeansFragment?= null
    private val mCameraOpenCloseLock = Semaphore(1)
    private var mImageReader: ImageReader? = null
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mCameraId: String? = null
    private var mCaptureSession: CameraCaptureSession? = null
            var  mFlashSupported: Boolean = false
    private var mSensorOrientation: Int = 0
    private var requestBuilder : CaptureRequest.Builder ?= null
    private var ORIENTATIONS =  SparseIntArray();
    private var previewRequest : CaptureRequest?= null
    private var mFile: File? = null

    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080
    private val TAG = "CameraHelper"

    enum class HELPER_STATE {
        STATE_NOT_INIT, STATE_INIT, STATE_PREVIEW, STATE_WAITING_LOCK, STATE_WAITING_PRECAPTURE, STATE_WAITING_NON_PRECAPTURE, STATE_PICTURE_TAKEN
    }

    private var state = HELPER_STATE.STATE_NOT_INIT

    init{
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    fun init(fragment: BeansFragment, fileName: String) {
        ownerFragment = fragment
        state = HELPER_STATE.STATE_INIT
        mFile = File(ownerFragment?.activity?.getExternalFilesDir(null), fileName)
    }

    fun deinit() {
        state = HELPER_STATE.STATE_NOT_INIT
    }

    @SuppressLint("MissingPermission")
    fun openCamera(surfaceTexture: AutofitTextureView?) {

        if(state == HELPER_STATE.STATE_INIT) {
            mSurfaceView = surfaceTexture

            mSurfaceView?.let {
                startBackgroundThread()

                setupCameraOutputs(it.width, it.height)
                configureTransform(it.width, it.height)

                val manager = ownerFragment?.activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                try {
                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        throw RuntimeException("Time out waiting to lock camera opening.")
                    }
                    manager.openCamera(mCameraId!!, cameraDeviceCallback, mBackgroundHandler)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    throw RuntimeException("Interrupted while trying to lock camera opening.", e)
                }
            }
        }
    }

    fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession?.close()
                mCaptureSession = null
            }

            if (null != mCamera) {
                mCamera?.close()
                mCamera = null
            }

            if (null != mImageReader) {
                mImageReader?.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }



    private val cameraDeviceCallback = object : CameraDevice.StateCallback() {

        override fun onDisconnected(camera: CameraDevice) {
            mCameraOpenCloseLock.release();
            mCamera?.close();
            mCamera = null;
        }

        override fun onError(camera: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release();
            mCamera?.close();
            mCamera = null;
        }


        override fun onOpened(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            mCamera = camera
            //Figure out what size the preview should be and what size the final jpg should be
            // to next step...
            //pass the surfaces to the camera
            val previewSurface = Surface(mSurfaceView?.surfaceTexture)
            val surfaces : List<Surface> = listOf(previewSurface, mImageReader?.surface!!)
            mCamera?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {

                override fun onConfigureFailed(session: CameraCaptureSession) {
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    mCaptureSession = session
                    requestBuilder = mCamera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    requestBuilder?.addTarget(previewSurface)
                    requestBuilder?.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    previewRequest = requestBuilder?.build()

                    session.setRepeatingRequest(previewRequest!!, captureCallback, mBackgroundHandler)
                }
            }, null)
        }

        override fun onClosed(camera: CameraDevice) {
            Log.d("In Camera Manager", "onClosed")
            stopBackgroundThread()
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            Log.d("in camera callback", state.name)
            when (state) {

                HELPER_STATE.STATE_PREVIEW -> {
                    // We have nothing to do when the camera preview is working normally.

                }

                HELPER_STATE.STATE_WAITING_LOCK -> {
                    var afState = result?.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        state = HELPER_STATE.STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    } else  if (CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState) {
                        //On some phones (Looking at you ZTE :()
                        //The af state does not move beyond CONTROL_AF_STATE_PASSIVE_FOCUSED
                        //So we nudge it to CONTROL_AF_STATE_FOCUSED_LOCKED
                        afState = CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                    }
                    if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result?.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            state = HELPER_STATE.STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                }

                HELPER_STATE.STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = HELPER_STATE.STATE_WAITING_NON_PRECAPTURE;
                    }
                }

                HELPER_STATE.STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = HELPER_STATE.STATE_PICTURE_TAKEN;
                        Log.d("in camera callback", "before taking a picture")
                        captureStillPicture();
                    }
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    private val imageAvailableListener = object : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            mBackgroundHandler?.post(ImageSaver(reader.acquireNextImage(), mFile!!, ownerFragment as CameraFragment))
        }
    }

    private fun setupCameraOutputs(width: Int, height: Int) {
        val manager = ownerFragment?.activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

                // For still image captures, we use the largest available size.
                val listOfSizes = map.getOutputSizes(ImageFormat.JPEG)
                val largest = Collections.max(
                    listOfSizes.asList(),
                    CompareSizesByArea())
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                    ImageFormat.JPEG, /*maxImages*/2)
                mImageReader?.setOnImageAvailableListener(
                    imageAvailableListener, mBackgroundHandler)

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = ownerFragment?.activity?.getWindowManager()?.getDefaultDisplay()?.getRotation()

                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation === 90 || mSensorOrientation === 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation === 0 || mSensorOrientation === 180) {
                        swappedDimensions = true
                    }
                    else -> Log.e(TAG, "Display rotation is invalid: $displayRotation")
                }

                val displaySize = Point()
                ownerFragment?.activity?.getWindowManager()?.getDefaultDisplay()?.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y

                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest)

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = ownerFragment?.getResources()?.getConfiguration()?.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mSurfaceView?.setAspectRatio(
                        mPreviewSize?.width!!, mPreviewSize?.height!!)
                } else {
                    mSurfaceView?.setAspectRatio(
                        mPreviewSize?.height!!, mPreviewSize?.width!!)
                }

                // Check if the flash is supported.
                val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                mFlashSupported = available ?: false

                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            //ErrorDialog.newInstance(getString(R.string.camera_error))
            //      .show(getChildFragmentManager(), FRAGMENT_DIALOG)
        }

    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == surfaceTexture || null == mPreviewSize || null == ownerFragment) {
            return
        }
        val rotation = ownerFragment?.activity!!.getWindowManager().getDefaultDisplay().getRotation()
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, (mPreviewSize?.height!!).toFloat(), (mPreviewSize?.width!!).toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / mPreviewSize?.height!!,
                viewWidth.toFloat() / mPreviewSize?.width!!)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180F, centerX, centerY)
        }
        mSurfaceView?.setTransform(matrix)
    }

    private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int,
                                  textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size
    ): Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for(option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if(bigEnough.size > 0) {
            return Collections.min(bigEnough, CompareSizesByArea())
        } else if(notBigEnough.size > 0) {
            return Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size")
            return choices[0]
        }
    }

    internal class CompareSizesByArea : Comparator<Size> {

        override fun compare(lhs: Size, rhs: Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.let {
            it.start()
            mBackgroundHandler = Handler(it.getLooper())
        }
    }

    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun takePicture() {
        //First wait for focus lock
        lockFocus()
    }

    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            requestBuilder?.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the lock.
            state = HELPER_STATE.STATE_WAITING_LOCK
            requestBuilder?.build()?.let {
                mCaptureSession?.capture(
                    it, captureCallback,
                    mBackgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            requestBuilder?.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            state = HELPER_STATE.STATE_WAITING_PRECAPTURE
            requestBuilder?.build()?.let {
                mCaptureSession?.capture(
                    it, captureCallback,
                    mBackgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun captureStillPicture() {
        try {
            if (null == ownerFragment?.activity || null == mCamera) {
                return
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = mCamera?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            mImageReader?.getSurface()?.let { captureBuilder?.addTarget(it) }

            // Use the same AE and AF modes as the preview.
            captureBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            //setAutoFlash(captureBuilder)

            // Orientation
            val rotation = ownerFragment?.activity!!.getWindowManager().getDefaultDisplay().getRotation()
            captureBuilder?.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult
                ) {
                    //Log.d(TAG, mFile.toString())
                    state = HELPER_STATE.STATE_INIT
                    //unlockFocus()
                }
            }
            mCaptureSession?.stopRepeating()
            mCaptureSession?.abortCaptures()
            captureBuilder?.build()?.let { mCaptureSession?.capture(it, CaptureCallback, null) }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            requestBuilder?.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            //setAutoFlash(requestBuilder)
            requestBuilder?.build()?.let {
                mCaptureSession?.capture(
                    it, captureCallback,
                    mBackgroundHandler)
            }
            // After this, the camera will go back to the normal state of preview.
            state = HELPER_STATE.STATE_PREVIEW
            mCaptureSession?.setRepeatingRequest(previewRequest!!, captureCallback,
                mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun getOrientation(rotation: Int): Int {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
    }

    fun setFlashState(on: Boolean) {
        if(on) {
            requestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

            requestBuilder?.set(CaptureRequest.FLASH_MODE,
                CaptureRequest.FLASH_MODE_TORCH)

            previewRequest = requestBuilder?.build()

            mCaptureSession?.setRepeatingRequest(previewRequest!!, captureCallback, mBackgroundHandler)

        } else {
            requestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

            requestBuilder?.set(CaptureRequest.FLASH_MODE,
                CaptureRequest.FLASH_MODE_OFF)

            previewRequest = requestBuilder?.build()

            mCaptureSession?.setRepeatingRequest(previewRequest!!, captureCallback, mBackgroundHandler)
        }
    }

    private class ImageSaver internal constructor(
        private val mImageJpeg: Image,
        private val mFile: File,
        private val ownerFragment: CameraFragment) : Runnable {

        override fun run() {
            val buffer = mImageJpeg.getPlanes()[0].getBuffer()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output!!.write(bytes)
                if(ownerFragment.isResumed && ownerFragment.isVisible) {
                    EventBus.getDefault().post(PictureCaptured(mFile.absolutePath))
                    /*var message = Message()
                    var bundle = Bundle()
                    bundle.putString("FILE", mFile.absolutePath)
                    message.what = CAMERA_PHOTO
                    message.data = bundle
                    ownerFragment.getHandler().sendMessage(message)*/
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImageJpeg.close()
                if (null != output) {
                    try {
                        output!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }


}