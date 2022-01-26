package ai.beans.common.widgets

import ai.beans.common.pojo.IconItem
import ai.beans.common.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

class BeansMarkerIconV2 : RelativeLayout {

    var markerIconIV: ImageView? = null
    var circularView: View?=null
    var selectedCircularView: View?=null
    var normalMarkerRL:RelativeLayout?=null
    var selectedMarkerRL: RelativeLayout?=null
    var cardView: CardView?=null
    var map: GoogleMap?=null
    var rightText: TextView? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, map: GoogleMap) : super(context)
    {
        this.map=map
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_marker_v2, this, true) as RelativeLayout
        markerIconIV = v.findViewById(R.id.markerIconView)
        circularView=v.findViewById(R.id.circularView)
        cardView=v.findViewById(R.id.container)
        selectedCircularView=v.findViewById(R.id.selectedBgView)
        normalMarkerRL=v.findViewById(R.id.normalMarkerView)
        selectedMarkerRL=v.findViewById(R.id.selectedMarkerView)
        rightText = v.findViewById(R.id.rightText)
    }

    suspend fun setMarkerContent(iconData: IconItem?, text : String?) {
        markerIconIV?.visibility = View.VISIBLE
        circularView?.visibility=View.VISIBLE
        var iconGenerator = IconGenerator(context)
        if(iconData != null) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
            var future = Glide.with(this)
                .asBitmap()
                .load(iconData.icon)
                .apply(requestOptions)

            var futureTarget = future.submit()

            var bmp : Bitmap ?= null
            var waitForLoad = MainScope().async(Dispatchers.IO) {
                bmp = futureTarget.get()
            }

            waitForLoad.await()

            if(bmp != null) {
                if(iconData.color_code != null) {
                    if(!iconData.color_code!!.startsWith("#")) {
                        iconData.color_code = "#" + iconData.color_code
                    }
                    circularView?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
                }

                markerIconIV?.setImageBitmap(bmp)

                if(text != null) {
                    setRightText(text!!)
                } else {
                    hideRightText()
                }
            }

        }
    }

    fun setRightText(text: String) {
        rightText?.visibility = View.VISIBLE
        rightText?.text = text
    }

    fun hideRightText() {
        rightText?.visibility = View.GONE
    }


}