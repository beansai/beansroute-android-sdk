package ai.beans.common.widgets

import ai.beans.common.R
import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.events.ShowDataEntryDialog
import ai.beans.common.pojo.search.*
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class RouteStopBeansDetails : FrameLayout {
    var isExpanded : Boolean = true
    var resizeBar : RelativeLayout ?= null
    var resizeIcon : ImageView ?= null
    var noteButtonListener : NoteButtonsListener ?= null
    var notesMap= HashMap<FeedbackNoteType, String?>()

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_stop_search_details, this, true)
        /*resizeIcon = v.findViewById(R.id.updownArrow)

        resizeBar = v.findViewById(R.id.resizeBar)
        resizeBar?.setOnClickListener {
            if(isExpanded) {
                EventBus.getDefault().post(CollapseCard())
            } else {
                EventBus.getDefault().post(ExpandCard())
            }
        }*/
        setupNotesPanel()
    }

    private fun setupNotesPanel() {
        var notesPanel = findViewById<RelativeLayout>(R.id.notesPanel)
        var noteTextView  = findViewById<TextView>(R.id.note)
        notesPanel?.setOnClickListener {
            var event = ShowDataEntryDialog(FeedbackNoteType.TEXT, noteTextView.text.toString())
            noteButtonListener?.let {
                it.noteWidgetClicked(event)
            }
        }
    }

    fun setPercentageTiles(tileInfo: ArrayList<PercentageTileInfo>?, customMarkerImagesViewModel: CustomMarkerImagesViewModel?) {
        var crimeDataPanel = findViewById<RelativeLayout>(R.id.crimeSliderPanel)
        var crimeDataProgressBar = findViewById<ProgressBar>(R.id.crimeProgressBar)
        var crimeLevelText = findViewById<TextView>(R.id.crimeLevelText)
        var crimeLevelTitle = findViewById<TextView>(R.id.crimeLevelTitle)
        var crimeIcon: ImageView = findViewById<ImageView>(R.id.crimeIcon)

        var tipsDataPanel = findViewById<RelativeLayout>(R.id.tipsSliderPanel)
        var tipsDataProgressBar = findViewById<ProgressBar>(R.id.tipsProgressBar)
        var tipsLevelText = findViewById<TextView>(R.id.tipsLevelText)
        var tipsLevelTitle = findViewById<TextView>(R.id.tipsLevelTitle)
        var tipsIcon: ImageView = findViewById<ImageView>(R.id.tipsIcon)

        if (tileInfo == null || tileInfo.size == 0) {
            var crimeAndTipsPanel = findViewById<RelativeLayout>(R.id.crimeAndTipsPanel)
            crimeAndTipsPanel?.visibility = View.GONE
            crimeDataPanel?.visibility = View.GONE
            tipsDataPanel?.visibility = View.GONE
            return
        }

        if (tileInfo!!.size >= 1) {
            crimeDataPanel?.visibility = View.VISIBLE

            crimeLevelText?.text = tileInfo!!.get(0).description
            crimeLevelTitle?.text = tileInfo!!.get(0).title
            var crimeLevel : Int = 0
            if(tileInfo!!.get(0).rate != null) {
                crimeLevel =  tileInfo!!.get(0).rate!!.toInt()
            }
            crimeDataProgressBar.progress = crimeLevel

            var iconData = customMarkerImagesViewModel?.getIconItemForType(tileInfo!!.get(0).type.toString())
            if (iconData?.color_code != null) {
                if(!iconData?.color_code!!.startsWith("#")) {
                    iconData?.color_code = "#" + iconData.color_code
                }
                crimeIcon?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
            }
            Glide.with(this)
                .asBitmap()
                .load(iconData?.icon)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        crimeIcon?.setImageBitmap(resource)
                    }
                })

            //set click listener
            crimeDataPanel.tag = getFeedbackNoteTypeFor(tileInfo!!.get(0).type!!)
            crimeDataPanel.setOnClickListener(bubbleClickListener)
        } else {
            crimeDataPanel?.visibility = View.GONE
        }

        if (tileInfo!!.size >= 2) {
            tipsDataPanel?.visibility = View.VISIBLE

            tipsLevelText?.text = tileInfo!!.get(1).description
            tipsLevelTitle?.text = tileInfo!!.get(1).title
            var tipsLevel : Int = 0
            if(tileInfo!!.get(1).rate != null) {
                tipsLevel =  tileInfo!!.get(1).rate!!.toInt()
            }
            tipsDataProgressBar.progress = tipsLevel

            var iconData = customMarkerImagesViewModel?.getIconItemForType(tileInfo!!.get(1).type.toString())
            if (iconData?.color_code != null) {
                if(!iconData?.color_code!!.startsWith("#")) {
                    iconData?.color_code = "#" + iconData.color_code
                }
                tipsIcon?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
            }
            Glide.with(this)
                .asBitmap()
                .load(iconData?.icon)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        tipsIcon?.setImageBitmap(resource)
                    }
                })

            //set click listener
            tipsDataPanel.tag = getFeedbackNoteTypeFor(tileInfo!!.get(1).type!!)
            tipsDataPanel.setOnClickListener(bubbleClickListener)
        } else {
            tipsDataPanel?.visibility = View.GONE
        }
    }

    fun setNote(noteList: NoteResponse?) {
        var notesPanel = findViewById<RelativeLayout>(R.id.notesPanel)
        var textView = findViewById<TextView>(R.id.note)
        if(noteList != null && !noteList.items.isNullOrEmpty()) {
            notesPanel?.visibility = View.VISIBLE
            for(note in noteList.items!!) {
                if(note.type == FeedbackNoteType.TEXT) {
                    textView.text = note.note
                }
                if(note.type!=null)
                {
                    notesMap.put(note.type!!, note.note)
                }
            }
        }
    }

    fun setInfoBubbles(bubbleList: ArrayList<TileInfo>?, customMarkerImagesViewModel: CustomMarkerImagesViewModel?) {
        var panel = findViewById<RelativeLayout>(R.id.beans_data_panel)
        var label : TextView?= null
        var bubble : RelativeLayout?= null

        if(bubbleList.isNullOrEmpty()) {
            panel?.visibility = View.GONE
        } else {
            panel?.visibility = View.VISIBLE

            // bubbles 1
            var bubbleData = bubbleList?.get(0)
            bubble = findViewById(R.id.bubble_1)
            bubble.tag = getFeedbackNoteTypeFor(bubbleData.type!!)
            label = findViewById(R.id.bubble_label_1)
            label.text = bubbleData?.text
            if(bubbleData.status.equals("UNKNOWN")) {
                label.setTextColor(resources.getColor(R.color.bubbleTextDisabled))
            } else {
                label.setTextColor(resources.getColor(R.color.bubbleText))
            }
            var icon1 : ImageView = findViewById(R.id.bubble_icon_1)
            var iconData = customMarkerImagesViewModel?.getIconItemForType(bubbleData.type.toString())
            if (bubbleData.status.equals("UNKNOWN")) {
                icon1?.background?.setColorFilter(Color.parseColor("#aaaaaa"), PorterDuff.Mode.ADD)
            } else if (iconData?.color_code != null) {
                if(!iconData?.color_code!!.startsWith("#")) {
                    iconData?.color_code = "#" + iconData.color_code
                }
                icon1?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
            }
            Glide.with(this)
                .asBitmap()
                .load(iconData?.icon)
                .apply(
                    RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        icon1?.setImageBitmap(resource)
                    }
                })
            // bubbles 2
            bubbleData = bubbleList.get(1)
            bubble = findViewById(R.id.bubble_2)
            bubble.tag = getFeedbackNoteTypeFor(bubbleData.type!!)
            label = findViewById(R.id.bubble_label_2)
            label.text = bubbleData?.text
            if(bubbleData.status.equals("UNKNOWN")) {
                label.setTextColor(resources.getColor(R.color.bubbleTextDisabled))
            } else {
                label.setTextColor(resources.getColor(R.color.bubbleText))
            }
            var icon2 : ImageView = findViewById(R.id.bubble_icon_2)
            iconData = customMarkerImagesViewModel?.getIconItemForType(bubbleData.type.toString())
            if (bubbleData.status.equals("UNKNOWN")) {
                icon2?.background?.setColorFilter(Color.parseColor("#aaaaaa"), PorterDuff.Mode.ADD)
            } else if (iconData?.color_code != null) {
                if(!iconData?.color_code!!.startsWith("#")) {
                    iconData?.color_code = "#" + iconData.color_code
                }
                icon2?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
            }
            Glide.with(this)
                .asBitmap()
                .load(iconData?.icon)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        icon2?.setImageBitmap(resource)
                    }
                })

            // bubbles 3
            bubbleData = bubbleList?.get(2)
            bubble = findViewById(R.id.bubble_3)
            bubble.tag = getFeedbackNoteTypeFor(bubbleData.type!!)
            label = findViewById(R.id.bubble_label_3)
            label.text = bubbleData?.text
            if(bubbleData.status.equals("UNKNOWN")) {
                label.setTextColor(resources.getColor(R.color.bubbleTextDisabled))
            } else {
                label.setTextColor(resources.getColor(R.color.bubbleText))
            }
            var icon3 : ImageView = findViewById(R.id.bubble_icon_3)
            iconData = customMarkerImagesViewModel?.getIconItemForType(bubbleData.type.toString())
            if (bubbleData.status.equals("UNKNOWN")) {
                icon3?.background?.setColorFilter(Color.parseColor("#aaaaaa"), PorterDuff.Mode.ADD)
            } else if (iconData?.color_code != null) {
                if(!iconData?.color_code!!.startsWith("#")) {
                    iconData?.color_code = "#" + iconData.color_code
                }
                icon3?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
            }
            Glide.with(this)
                .asBitmap()
                .load(iconData?.icon)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(object: CustomTarget<Bitmap>(){
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        icon3?.setImageBitmap(resource)
                    }
                })
        }

        //finally, set the click listeners for each bubble
        var bubble1 = findViewById<RelativeLayout>(R.id.bubble_1)
        bubble1.setOnClickListener(bubbleClickListener)

        var bubble2 = findViewById<RelativeLayout>(R.id.bubble_2)
        bubble2.setOnClickListener(bubbleClickListener)

        var bubble3 = findViewById<RelativeLayout>(R.id.bubble_3)
        bubble3.setOnClickListener(bubbleClickListener)
    }

    fun getFeedbackNoteTypeFor(type: MapMarkerType ): FeedbackNoteType {
        when(type) {
            MapMarkerType.UNIT -> {
                return FeedbackNoteType.UNIT
            }
            MapMarkerType.PARKING -> {
                return FeedbackNoteType.PARKING
            }
            MapMarkerType.ENTRANCE -> {
                return FeedbackNoteType.ENTRANCE
            }
            MapMarkerType.ELEVATOR -> {
                return FeedbackNoteType.ELEVATOR
            }
            MapMarkerType.STAIR -> {
                return FeedbackNoteType.STAIR
            }
            MapMarkerType.CRIME -> {
                return FeedbackNoteType.CRIME
            }
            MapMarkerType.TIP -> {
                return FeedbackNoteType.TIP
            }
            MapMarkerType.LOBBY -> {
                return FeedbackNoteType.LOBBY
            }
            MapMarkerType.VISITOR_PARKING -> {
                return FeedbackNoteType.PARKING
            }
            MapMarkerType.CURB_PARKING -> {
                return FeedbackNoteType.PARKING
            }
            MapMarkerType.HANDICAP_RAMP -> {
                return FeedbackNoteType.ENTRANCE
            }
            MapMarkerType.SERVICE_ELEVATOR -> {
                return FeedbackNoteType.ELEVATOR
            }
            MapMarkerType.EMERGENCY_EXIT -> {
                return FeedbackNoteType.ENTRANCE
            }
            MapMarkerType.EMERGENCY_STAIR -> {
                return FeedbackNoteType.STAIR
            }
            MapMarkerType.FIRE_HYDRANT -> {
                return FeedbackNoteType.TEXT
            }
            MapMarkerType.LEASING_OFFICE -> {
                return FeedbackNoteType.LOBBY
            }
            MapMarkerType.MAILBOX -> {
                return FeedbackNoteType.LOBBY
            }
            MapMarkerType.DELIVERY_LOCKER -> {
                return FeedbackNoteType.LOBBY
            }
        }
        return FeedbackNoteType.TEXT
    }

    var bubbleClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var event :ShowDataEntryDialog ?= null
            var noteData:String?=fetchNoteDetails(v?.tag as FeedbackNoteType)

            event = ShowDataEntryDialog(v.tag as FeedbackNoteType, noteData)
            if(event != null && noteButtonListener != null) {
                noteButtonListener!!.noteWidgetClicked(event)
            }
        }
    }

    private fun fetchNoteDetails(type:FeedbackNoteType): String?
    {
        return notesMap.get(type)
    }

    fun clearNotesMap() {
        notesMap.clear()
    }
}