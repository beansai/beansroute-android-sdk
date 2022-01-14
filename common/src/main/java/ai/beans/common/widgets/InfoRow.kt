package ai.beans.common.widgets
import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

class InfoRow : RelativeLayout {
    var imageButton : ImageButton ?= null
    var info : TextView ?= null


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.info_row, this, true)
        setup()
    }

    private fun setup() {
        imageButton = findViewById(R.id.leftImageButton)
        info = findViewById(R.id.info)

    }

    fun setInfo(info : String) {
        this.info?.text = info
    }

    fun setLeftImage(imageId : Int) {
        imageButton?.background = context.getDrawable(imageId)
        imageButton?.visibility = View.VISIBLE
    }


}