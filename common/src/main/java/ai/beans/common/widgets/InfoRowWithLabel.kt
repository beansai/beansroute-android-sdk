package ai.beans.common.widgets

import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class InfoRowWithLabel : RelativeLayout {
    var imageButton : ImageView?= null
    var label : TextView ?= null
    var info : TextView ?= null


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.info_row_with_label, this, true)
        setup()
    }

    private fun setup() {
        imageButton = findViewById(R.id.rightImageButton)
        label = findViewById(R.id.label)
        info = findViewById(R.id.info)
    }

    fun setLabel(label : String) {
        this.label?.text = label
    }

    fun setInfo(info : String) {
        this.info?.text = info
    }

    fun setButtonImage(imageId : Int) {
        imageButton?.background = context.getDrawable(imageId)
    }


}