package ai.beans.common.widgets.camera

import ai.beans.common.R
import ai.beans.common.SourceCamera
import ai.beans.common.SourceGallery
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus

class ImageSourcePickerSheet : BottomSheetDialogFragment() {
    private var cameraButton: LinearLayout?= null
    private var galleryButton: LinearLayout?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_image_source_picker, container, false)

        cameraButton = v.findViewById<LinearLayout>(R.id.camera)
        galleryButton = v.findViewById<LinearLayout>(R.id.gallery)

        cameraButton?.setOnClickListener {
            dismiss()
            EventBus.getDefault().post(SourceCamera)
        }

        galleryButton?.setOnClickListener {
            dismiss()
            EventBus.getDefault().post(SourceGallery)
        }

        return v
    }

    override fun onResume() {
        super.onResume()
    }
}