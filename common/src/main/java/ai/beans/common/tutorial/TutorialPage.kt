package ai.beans.common.tutorial


import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView


class TutorialPage : RelativeLayout {

    var attributes : AttributeSet ?= null
    var imageDrawable : Drawable ?= null
    var majorText : String ?= null
    var minorText : String ?= null

    var centerImage : ImageView ?= null
    var majorTextView : TextView ?= null
    var minorTextView : TextView ?= null

    var majorTextColor : Int ?= null
    var minorTextColor : Int ?= null


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        attributes = attrs
        val a = context?.obtainStyledAttributes(attributes, R.styleable.TutorialPage, 0, 0)
        imageDrawable = a?.getDrawable(R.styleable.TutorialPage_center_image_drawable_id)
        majorText = a?.getString(R.styleable.TutorialPage_major_text)
        minorText = a?.getString(R.styleable.TutorialPage_minor_text)
        majorTextColor = a?.getColor(R.styleable.TutorialPage_tutorial_major_string_color, resources.getColor(R.color.colorWhite))
        minorTextColor = a?.getColor(R.styleable.TutorialPage_tutorial_minor_string_color, resources.getColor(R.color.colorWhite))
        setupContent()

    }

    private fun setupContent() {
        centerImage?.setBackgroundDrawable(imageDrawable)
        majorTextView?.text = majorText
        majorTextView?.setTextColor(majorTextColor!!)
        minorTextView?.text = minorText
        minorTextView?.setTextColor(minorTextColor!!)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.tutorial_page, this, true)

        centerImage = findViewById<ImageView>(R.id.tutorialImage)
        majorTextView = findViewById<TextView>(R.id.majorText)
        minorTextView = findViewById<TextView>(R.id.minorText)
    }

}