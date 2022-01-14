package ai.beans.common.widgets.imagedisplay

import android.graphics.Point

interface BeansGestureDetectorListener {
    fun onSingleTap(majorPoint: Point?)
    fun onPinchZoomStart(
        majorPointStart: Point?,
        minorPointStart: Point?
    )

    fun onPinchZoom(
        majorPointPrev: Point?,
        majorPointCurrent: Point?,
        minorPointPrev: Point?,
        minorPointCurrent: Point?,
        pinchDistance: Float
    )

    fun onDragStart(startPoint: Point?)
    fun onDrag(
        pointPrev: Point?,
        pointCurrent: Point?
    )

    fun onDragEnd()
    fun onPinchZoomEnd()
    fun onActionEnd()
    fun onMultiTapStart()
    fun onLongPress()
}