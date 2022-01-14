package ai.beans.common.widgets.imagedisplay

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import java.util.ArrayList

class BeansImageSurfaceView(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs),
    SurfaceHolder.Callback , BeansGestureDetectorListener {

    private var bitmap: Bitmap? = null
    private var parentWidth: Int = 0
    private var parentHeight: Int = 0
    private var xScaleFactor: Float = 0f
    private var minXScaleFactor: Float = 0f
    private var maxXScaleFactor: Float = 3f
    private var xTranslate: Float = 0f
    private var yTranslate: Float = 0f

    private var xBaselineTranslate: Float = 0f
    private var yBaselineTranslate: Float = 0f

    private var xCenter: Float = 0f
    private var yCenter: Float = 0f

    private var currentMatrix = Matrix()
    internal var gestureDetector: BeansGestureDetector? = null
    var scaleFactorNormalizer: Float = 0f

    internal var animatorSet: AnimatorSet? = null
    private val ANIMATION_DURATION = 500
    private var isSurfaceOK = false


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (bitmap != null) {
            val canvas = holder.lockCanvas()
            doDraw(bitmap!!, canvas)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isSurfaceOK = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        setWillNotDraw(false)
        isSurfaceOK = true
        gestureDetector = BeansGestureDetector(this, this)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if(visibility == View.VISIBLE)
        else
            isSurfaceOK = false
    }

    fun processBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap

        if (bitmap!!.width > bitmap!!.height) {
            xScaleFactor = (parentWidth.toFloat() / bitmap!!.width.toFloat())
            //yScaleFactor = (parentHeight.toFloat()/bitmap!!.width.toFloat())
        } else {
            xScaleFactor = (parentHeight.toFloat() / bitmap!!.height.toFloat())
            //yScaleFactor = (parentHeight.toFloat()/bitmap!!.height.toFloat())
        }

        minXScaleFactor = xScaleFactor

        //translation?
        val displayWidth = (bitmap!!.width * xScaleFactor).toInt()
        val displayHeight = (bitmap!!.height * xScaleFactor).toInt()

        if (displayHeight != parentHeight) {
            //center image horizontally
            yTranslate = ((parentHeight - displayHeight) / 2).toFloat()
            yBaselineTranslate = yTranslate
            xTranslate = 0f
            xBaselineTranslate = 0f
        } else if (displayWidth != parentWidth) {
            //center image vertically
            xTranslate = ((parentWidth - displayWidth) / 2).toFloat()
            xBaselineTranslate = xTranslate
            yTranslate = 0f
            yBaselineTranslate = 0f
        }

        currentMatrix.reset()
        currentMatrix.preScale(xScaleFactor, xScaleFactor)
        currentMatrix.postTranslate(xTranslate, yTranslate)
    }

    fun setupRenderMatrix() {
        currentMatrix.reset()
        currentMatrix.preScale(xScaleFactor, xScaleFactor, xCenter, yCenter)
        currentMatrix.postTranslate(xTranslate, yTranslate)
    }

    init {
        holder.addCallback(this)
        scaleFactorNormalizer = getScaleFactorNormalizer(context!!)
    }

    private fun doDraw(bitmap: Bitmap, canvas: Canvas?) {

        val paint = Paint()
        paint.isFilterBitmap = true
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas?.drawBitmap(bitmap, currentMatrix, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        parentHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        this.setMeasuredDimension(parentWidth, parentHeight)
    }

    fun renderBitmap() {
        if (bitmap != null && isSurfaceOK) {
            holder?.surface?.isValid
            val canvas = holder?.lockCanvas()
            setupRenderMatrix()
            doDraw(bitmap!!, canvas)
            holder?.unlockCanvasAndPost(canvas)
        }
    }

    fun resetImage() {
        //This method takes the image to its final state after an action (pinchZoom/Drag)
        val screenRect = RectF(0f, 0f, parentWidth.toFloat(), parentHeight.toFloat())
        val rect = RectF(0f, 0f, bitmap!!.getWidth().toFloat(), bitmap!!.getHeight().toFloat())

        var xTrans = 0f
        var yTrans = 0f
        var matrix = Matrix()
        matrix.reset()
        matrix.preScale(xScaleFactor, xScaleFactor, xCenter, yCenter)
        matrix.postTranslate(xTranslate, yTranslate)
        matrix.mapRect(rect)

        var to = Point()
        var from = Point()
        var needsRest = false

        from.x = 0
        from.y = 0

        //Check if the

        val contains = screenRect.contains(rect)

        if(!contains) {
            var xTransCandidate1 = 0f
            var xTransCandidate2 = 0f
            if(rect.left > 0 && rect.right > parentWidth) {
                xTransCandidate1 = -rect.left
                xTransCandidate2 = (parentWidth - rect.right)
                needsRest = true
            } else if(rect.left < 0 && rect.right < parentWidth) {
                xTransCandidate1 = -rect.left
                xTransCandidate2 = (parentWidth - rect.right)
                needsRest = true
            }

            xTrans = if(Math.abs(xTransCandidate1) <= Math.abs(xTransCandidate2)) xTransCandidate1 else xTransCandidate2

            var yTransCandidate1 = 0f
            var yTransCandidate2 = 0f

            if(rect.top > 0 && rect.bottom > parentHeight) {
                yTransCandidate1 = -rect.top
                yTransCandidate2 = (parentHeight - rect.bottom)
                needsRest = true
            } else if(rect.top < 0 && rect.bottom < parentHeight) {
                yTransCandidate1 = -rect.top
                yTransCandidate2 = (parentHeight - rect.bottom)
                needsRest = true
            }

            yTrans = if(Math.abs(yTransCandidate1) <= Math.abs(yTransCandidate2)) yTransCandidate1 else yTransCandidate2

            if (needsRest) {
                to.x = xTrans.toInt()
                to.y = yTrans.toInt()
                setValueAnimator(from, to)
            }
        }

    }

    override fun onSingleTap(majorPoint: Point?) {
    }

    override fun onPinchZoomStart(majorPointStart: Point?, minorPointStart: Point?) {
        //find the center
        var tapCenterX = (minorPointStart!!.x + majorPointStart!!.x)/2
        var tapCenterY = (minorPointStart!!.y + majorPointStart!!.y)/2

        //This is the point on screen....where does it lie on the bitmap
        var floatArray = FloatArray(2)
        floatArray[0] = tapCenterX.toFloat()
        floatArray[1] = tapCenterY.toFloat()
        var matrix = Matrix()
        matrix.reset()
        matrix.preTranslate(-xTranslate, -yTranslate)
        matrix.postScale(1/xScaleFactor, 1/xScaleFactor, xCenter, yCenter)
        matrix.mapPoints(floatArray)

        xCenter = floatArray[0]
        yCenter = floatArray[1]

        xTranslate = tapCenterX - xCenter
        yTranslate = tapCenterY - yCenter

    }

    override fun onPinchZoom(majorPointPrev: Point?, majorPointCurrent: Point?, minorPointPrev: Point?, minorPointCurrent: Point?, pinchDistance: Float) {
        var newXScaleFactor = Math.abs(pinchDistance / scaleFactorNormalizer)
        if (pinchDistance >= 0) {

            if (xScaleFactor + newXScaleFactor > maxXScaleFactor) {
                xScaleFactor = maxXScaleFactor
            } else {
                xScaleFactor += newXScaleFactor
            }
        } else {
            if (xScaleFactor - newXScaleFactor < minXScaleFactor) {
                xScaleFactor = minXScaleFactor
            } else {
                xScaleFactor -= newXScaleFactor
            }
        }
        renderBitmap()

    }

    override fun onDragStart(startPoint: Point?) {
        if (animatorSet != null) {
            animatorSet?.removeAllListeners()
            animatorSet?.cancel()
        }
    }

    override fun onDrag(pointPrev: Point?, pointCurrent: Point?) {
        val xoffset = pointCurrent!!.x - pointPrev!!.x
        val yoffset = pointCurrent!!.y - pointPrev!!.y
        xTranslate += xoffset
        yTranslate += yoffset
        renderBitmap()
    }

    override fun onDragEnd() {
    }

    override fun onPinchZoomEnd() {
    }

    override fun onActionEnd() {
        resetImage()
    }

    override fun onMultiTapStart() {
    }

    override fun onLongPress() {
    }

    fun getScaleFactorNormalizer(context: Context): Float {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return (metrics.widthPixels * 30 / 100).toFloat()
    }

    fun setValueAnimator(from: Point, to: Point) {
        val path: Path
        val measure: PathMeasure
        val pos: FloatArray
        val tan: FloatArray
        val points: Array<Point?>
        pos = FloatArray(2)
        tan = FloatArray(2)
        val set = AnimatorSet()
        val translation = ValueAnimator()

        path = Path()
        path.moveTo(from.x.toFloat(), from.y.toFloat())
        path.cubicTo(from.x.toFloat(), from.y.toFloat(), to.x.toFloat(), to.y.toFloat(), to.x.toFloat(), to.y.toFloat())
        measure = PathMeasure(path, false)
        points = arrayOfNulls(measure.length.toInt())
        val length = measure.length.toInt()

        for (i in 0 until length) {
            measure.getPosTan(i.toFloat(), pos, tan)
            points[i] = Point()
            points[i]!!.x = pos[0].toInt()
            points[i]!!.y = pos[1].toInt()

            //Log.d("XTrans Point", points[i]!!.x.toString())
        }
        val xTrans = xTranslate
        val yTrans = yTranslate
        translation.setObjectValues(*points)
        translation.duration = ANIMATION_DURATION.toLong()
        translation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Point
            //Log.d("XTrans Pre", xTranslate.toString())
            xTranslate = xTrans + value.x.toFloat()
//            Log.d("XTrans Post", xTranslate.toString())
            yTranslate  = yTrans + value.y.toFloat()
            renderBitmap()
        }
        translation.setEvaluator { v, start, end -> end }
        if (points.size > 0) {
            val animatorList = ArrayList<Animator>()
            if (translation != null) animatorList.add(translation)
            animatorSet = AnimatorSet()
            animatorSet?.playTogether(animatorList)
            animatorSet?.start()
        }
    }
}