package ai.beans.common.tutorial

import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter



class PagePositionIndicator : LinearLayout {

    var selected_Color: Int?= null
    var unselect_color: Int ?= null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.PagePositionIndicator, 0, 0)
        selected_Color = a?.getColor(R.styleable.PagePositionIndicator_selected_dot_color, resources.getColor(R.color.colorBlack))
        unselect_color = a?.getColor(R.styleable.PagePositionIndicator_unselected_dot_color, resources.getColor(R.color.colorWhite))
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        orientation = HORIZONTAL
    }

    fun setup(size: Int) {
        removeAllViews()
        for (i in 0 until size) {
            val imgView = View(context)
            val lp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.height = dipToPixels(context, 10F).toInt()
            lp.width = dipToPixels(context, 10F).toInt()
            val offset = dipToPixels(context, 6F).toInt()
            lp.setMargins(offset, offset, offset, offset)
            imgView.layoutParams = lp
            imgView.background = context.getDrawable(R.drawable.indicator_circle)
            addView(imgView)
        }

    }

    fun dipToPixels(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }

    fun update(position: Int) {
        val size = childCount
        for (i in 0 until size) {
            val imgView = getChildAt(i) as View
            var drawable = imgView.background
            if (i == position) {
                if(drawable != null) {
                    drawable.setColorFilter(PorterDuffColorFilter(selected_Color!!, PorterDuff.Mode.SRC_IN))
                }
            } else {
                if (drawable != null) {
                    drawable.setColorFilter(PorterDuffColorFilter(unselect_color!!, PorterDuff.Mode.SRC_IN))
                }
            }

        }
    }

}