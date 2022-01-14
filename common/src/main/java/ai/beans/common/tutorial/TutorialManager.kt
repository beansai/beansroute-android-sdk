package ai.beans.common.tutorial

import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ViewFlipper


class TutorialManager : ViewFlipper, GestureDetector.OnGestureListener {

    var gestureDetector : GestureDetector ?= null
    var actionListener : TutorialPageActionListener?= null

    private val MIN_DISTANCE = 190
    private val SWIPE_VELOCITY = 250
    private var currentPageIndex = 0;

    constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        }


    init {
        gestureDetector = GestureDetector(context, this)
        setForwardAnimation()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (gestureDetector!!.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }    }


    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (e1!!.x - e2!!.x > MIN_DISTANCE && Math.abs(velocityX) > SWIPE_VELOCITY) {
            onRightToLeft();
            return true;
        } else if (e2.x - e1.x > MIN_DISTANCE && Math.abs(velocityX) > SWIPE_VELOCITY) {
            onLeftToRight();
            return true;
        }
        return false;
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return true
    }


    fun onRightToLeft() {
        if(currentPageIndex < childCount - 1) {
            setForwardAnimation()
            showNext()
            currentPageIndex++
            actionListener?.onNewPageInView(currentPageIndex)
        }
    }

    fun onLeftToRight() {
        if(currentPageIndex > 0) {
            setBackwardAnimation()
            showPrevious()
            currentPageIndex--
            actionListener?.onNewPageInView(currentPageIndex)
        }
    }

    fun setForwardAnimation() {
        val imgAnimationIn = AnimationUtils.loadAnimation(context, R.anim.forward_slide_in)
        inAnimation = imgAnimationIn
        val imgAnimationOut = AnimationUtils.loadAnimation(context, R.anim.forward_slide_out)
        outAnimation = imgAnimationOut

    }

    fun setBackwardAnimation() {
        val imgAnimationIn = AnimationUtils.loadAnimation(context, R.anim.backward_slide_in)
        inAnimation = imgAnimationIn
        val imgAnimationOut = AnimationUtils.loadAnimation(context, R.anim.backward_slide_out)
        outAnimation = imgAnimationOut
    }

    fun setListener(listener: TutorialPageActionListener) {
        actionListener = listener
    }

    fun moveToNextPage() {
        onRightToLeft()
    }

}