package ai.beans.common.location

import ai.beans.common.ui.core.BeansActivity
import android.os.Bundle

class DummyLaunchActivity : BeansActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()

    }
}