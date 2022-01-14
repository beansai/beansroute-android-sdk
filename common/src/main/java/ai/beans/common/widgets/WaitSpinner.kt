package ai.beans.common.widgets

import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout

class WaitSpinner : RelativeLayout {

    var waitSpinner : ProgressBar ?= null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.wait_spinner_container, this, true)
        waitSpinner = findViewById(R.id.progress_circular)

    }

    fun show() {
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }

}