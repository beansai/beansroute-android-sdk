package ai.beans.common.widgets.camera

import ai.beans.common.utils.ObservableData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraCaptureResults : ViewModel() {
    //GOAL:Using the ViewModel infra to implement a results exchange between fragments
    //The view model hangs off the activity's lifecycle.
    //Each fragment can register and observer that will be called when there are results for that fragment

    //For now...this just handles images from gallery/camera
    var imageCaptureResult = MutableLiveData<ObservableData<ImageCaptureResult>>()
}