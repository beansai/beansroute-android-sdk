package ai.beans.common.widgets

import ai.beans.common.R
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView

class HUD : Dialog {

    var message : TextView ?= null
    var progressBar : ProgressBar?= null
    var checkmark : ImageView?= null

    constructor(context: Context) : super(context, R.style.Hud) {
        val layout = layoutInflater.inflate(R.layout.hud_layout, null) as RelativeLayout
        setContentView(layout)
        message = layout.findViewById(R.id.hud_message)
        progressBar = layout.findViewById(R.id.progress_circular)
        checkmark = layout.findViewById(R.id.checkMark)
    }

    fun setText(text: String?) {
        if(text != null) {
            message?.visibility = View.VISIBLE
            message?.setText(text)
        } else {
            message?.visibility = View.GONE
        }
    }

    fun showProgressBar() {
        checkmark?.visibility = View.GONE
        progressBar?.visibility = View.VISIBLE
    }
    fun showSuccessMark() {
        progressBar?.visibility = View.GONE
        checkmark?.visibility = View.VISIBLE
    }
}