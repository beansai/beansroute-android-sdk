package ai.beans.common.maps

import ai.beans.common.R
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout

class RoundMapButton : RelativeLayout {

    var isButtonPressed : Boolean = false
    var imageView : ImageView?= null
    var imageDown : Drawable?= null
    var imageUp : Drawable?= null
    var backroundDown : Drawable?= null
    var backroundUp : Drawable?= null
    var mapButtonlistener : MapButtonListener ?= null


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.RoundMapButton, 0, 0)
        backroundDown = a?.getDrawable(R.styleable.RoundMapButton_button_down_background)
        backroundUp = a?.getDrawable(R.styleable.RoundMapButton_button_up_background)
        imageDown = a?.getDrawable(R.styleable.RoundMapButton_button_down_image)
        imageUp = a?.getDrawable(R.styleable.RoundMapButton_button_up_image)

        setupContent()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }


    private fun setupContent() {
        setBackgroundDrawable(backroundUp)
        imageView?.setBackgroundDrawable(imageUp)
    }


    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_map_btn_round, this, true) as RelativeLayout
        imageView = v.findViewById(R.id.innerImageView)
        setOnClickListener {
            if(mapButtonlistener != null) {
                mapButtonlistener?.buttonClicked(id, isButtonPressed)
            }
        }
    }

    fun enableButton() {
        if(!isButtonPressed) {
            toggleButton()
        }
    }

    fun disableButton() {
        if(isButtonPressed) {
            toggleButton()
        }
    }

    fun toggleButton() {
        isButtonPressed = !isButtonPressed
        if(isButtonPressed) {
            setBackgroundDrawable(backroundDown)
            imageView?.setBackgroundDrawable(imageDown)

        } else {
            setBackgroundDrawable(backroundUp)
            imageView?.setBackgroundDrawable(imageUp)
        }
    }

    interface MapButtonListener {
        fun buttonClicked(btnId: Int, state : Boolean)
    }

}