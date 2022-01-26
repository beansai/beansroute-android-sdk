package ai.beans.common.custom_markers

import ai.beans.common.R
import ai.beans.common.events.MarkerRenderComplete
import ai.beans.common.pojo.IconItem
import ai.beans.common.pojo.custom_markers.MarkerNoteData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import org.greenrobot.eventbus.EventBus

class BeansCustomMarker : RelativeLayout {

    var markerIconIV: ImageView? = null
    var circularView: View?=null
    var selectedCircularView: View?=null
    var normalMarkerRL: RelativeLayout?=null
    var selectedMarkerRL: RelativeLayout?=null
    var cardView: CardView?=null
    var map: GoogleMap?=null

    constructor(context: Context) : super(context)

    constructor(context: Context, map: GoogleMap) : super(context)
    {
        this.map=map
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.view_custom_marker, this, true) as RelativeLayout
        markerIconIV = v.findViewById(R.id.markerIconView)
        circularView=v.findViewById(R.id.circularView)
        cardView=v.findViewById(R.id.container)
        selectedCircularView=v.findViewById(R.id.selectedBgView)
        normalMarkerRL=v.findViewById(R.id.normalMarkerView)
        selectedMarkerRL=v.findViewById(R.id.selectedMarkerView)
    }

    fun setupMarkerListIcon(iconData: IconItem?=null)
    {
        markerIconIV?.visibility = View.VISIBLE
        circularView?.visibility= View.VISIBLE
        cardView?.elevation=0f
        cardView?.setCardBackgroundColor(Color.TRANSPARENT)
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        if(iconData != null) {
            Glide.with(context).load(iconData.icon).apply(requestOptions).into(markerIconIV!!)
        }
        if(iconData?.color_code != null) {
            if(!iconData.color_code!!.startsWith("#")) {
                iconData.color_code = "#" + iconData.color_code
            }
            circularView?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
        }
    }

    fun setupMarkerView(iconData: IconItem?=null, markerNoteData: MarkerNoteData, shouldSaveToServer:Boolean = false) {
        markerIconIV?.visibility = View.VISIBLE
        circularView?.visibility= View.VISIBLE
        var marker: Marker?
        var iconGenerator = IconGenerator(context)
        if(iconData != null) {
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
            Glide.with(this)
                .asBitmap()
                .load(iconData.icon)
                .apply(requestOptions)
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if(iconData.color_code != null) {
                            if(!iconData.color_code!!.startsWith("#")) {
                                iconData.color_code = "#" + iconData.color_code
                            }
                            circularView?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
                        }

                        markerIconIV?.setImageBitmap(resource)
                        val transparentDrawable = ColorDrawable(Color.TRANSPARENT)
                        iconGenerator.setContentView(this@BeansCustomMarker)
                        iconGenerator.setBackground(transparentDrawable)
                        val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())
                        val markerOptions = MarkerOptions().position(LatLng(markerNoteData.point?.lat!!, markerNoteData.point?.lng!!))
                            .icon(icon)
                            .visible(true)
                            .draggable(true)
                            .zIndex(100.0f)

                        marker = map?.addMarker(markerOptions)
                        marker?.tag = markerNoteData
                        EventBus.getDefault().post(MarkerRenderComplete(marker!!, shouldSaveToServer))
                    }
                })
        }
    }

    fun updateMarkerView(marker : Marker, iconData: IconItem?=null, isLargeIcon: Boolean) {
        markerIconIV?.visibility = View.VISIBLE
        var iconGenerator = IconGenerator(context)
        if(iconData != null) {
            Glide.with(this)
                .asBitmap()
                .load(iconData.icon)
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        markerIconIV?.setImageBitmap(resource)
                        var markerView=this@BeansCustomMarker
                        if(isLargeIcon)
                        {

                            if(iconData.color_code != null) {
                                if(!iconData.color_code!!.startsWith("#")) {
                                    iconData.color_code = "#" + iconData.color_code
                                }
                                selectedCircularView?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
                            }
                            normalMarkerRL?.visibility= View.GONE
                            selectedCircularView?.visibility= View.VISIBLE
                            selectedMarkerRL?.visibility= View.VISIBLE
                        }
                        else
                        {
                            if(iconData.color_code != null) {
                                if(!iconData.color_code!!.startsWith("#")) {
                                    iconData.color_code = "#" + iconData.color_code
                                }
                                circularView?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
                            }
                            normalMarkerRL?.visibility= View.VISIBLE
                            circularView?.visibility= View.VISIBLE
                            selectedMarkerRL?.visibility= View.GONE
                        }
                        val transparentDrawable = ColorDrawable(Color.TRANSPARENT)
                        iconGenerator.setContentView(markerView)
                        iconGenerator.setBackground(transparentDrawable)
                        val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())
                        marker.setIcon(icon)
                    }
                })
        }
    }
}