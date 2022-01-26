package ai.beans.common.application

import android.app.Application
import android.content.Context

class BeansContextContainer {
    companion object {
        var application: Application? = null
        var context: Context? = null

        fun setupViewModels() {}
    }
}