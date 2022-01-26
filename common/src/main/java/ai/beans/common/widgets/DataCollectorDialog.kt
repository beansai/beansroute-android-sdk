package ai.beans.common.widgets

import ai.beans.common.custom_markers.CustomMarkerImagesViewModel
import ai.beans.common.networking.Envelope
import ai.beans.common.pojo.search.FeedbackNoteType
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.R
import ai.beans.common.networking.isp.postAddressNotes
import ai.beans.common.pojo.search.NoteItem
import ai.beans.common.pojo.search.NoteResponse
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class DataCollectorDialog : BeansFragment() {

    var bus: EventBus?= null
    var note : String ?= null
    var editText : EditText?= null
    var cancel : TextView?= null
    var submit : TextView?= null
    var queryId : String ?= null
    var icon: ImageView ?= null
    var title: TextView ?= null
    var instructions: TextView ?= null
    var noteType : FeedbackNoteType ?= null
    var screen_name : String ?= null
    private var customMarkerImagesViewModel : CustomMarkerImagesViewModel?= null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)

        customMarkerImagesViewModel = ViewModelProviders.of(activity!!).get(CustomMarkerImagesViewModel::class.java)

        val v = LayoutInflater.from(context).inflate(R.layout.data_collector_dialog, null)
        alertDialogBuilder.setView(v as View)

        val alertDialog = alertDialogBuilder.create()

        editText = v.findViewById(R.id.notes_edit_box)
        if(note!=null)
        {
            editText?.setText(note)
        }
        cancel = v.findViewById(R.id.cancel)
        submit = v.findViewById(R.id.submit)

        icon = v.findViewById(R.id.typeIcon)
        title = v.findViewById(R.id.title)
        instructions = v.findViewById(R.id.instructions)


        cancel?.setOnClickListener {
            dismiss()
        }

        submit?.setOnClickListener {
            if(queryId != null && noteType != null) {
                var note = ""
                if (editText?.text != null) {
                    note = editText?.text.toString().trim()
                }

                var noteItem = NoteItem()
                noteItem.note = note
                noteItem.type = noteType

                var noteArray = ArrayList<NoteItem>()
                noteArray.add(noteItem)

                var bodyMap = HashMap<String, ArrayList<NoteItem>>()
                bodyMap.put("items", noteArray)

                MainScope().launch {
                    showHUD()
                    var noteResponse : Envelope<NoteResponse>?= null
                    var fetchNotes = MainScope().async(Dispatchers.IO) {
                        noteResponse = postAddressNotes(queryId!!, bodyMap)
                    }
                    fetchNotes.await()
                    if(noteResponse != null && noteResponse!!.success) {
                        bus?.post(noteResponse!!.data)
                    }
                    hideHUD()
                    dismiss()
                }
            }
        }

        setDialogStyle()

        return alertDialog
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    fun setDialogStyle() {
        var iconData = customMarkerImagesViewModel?.getIconItemForType(noteType.toString())
        if (iconData?.color_code != null) {
            if(!iconData?.color_code!!.startsWith("#")) {
                iconData?.color_code = "#" + iconData.color_code
            }
            icon?.background?.setColorFilter(Color.parseColor(iconData.color_code), PorterDuff.Mode.ADD)
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
                    icon?.setImageBitmap(resource)
                }
            })

        screen_name = noteType.toString().toLowerCase() + "_notes_dlg"
        title?.text = "Info"
        instructions?.text = "Enter a note"

        when(noteType) {
            FeedbackNoteType.ELEVATOR -> {
                title?.text = "Elevator Info"
                instructions?.text = "Enter a note regarding elevators for this building"
            }

            FeedbackNoteType.STAIR -> {
                title?.text = "Stair Info"
                instructions?.text = "Enter a note regarding stairs for this building"
            }

            FeedbackNoteType.ENTRANCE -> {
                title?.text = "Entrance Info"
                instructions?.text = "Enter a note regarding entrance for this building. or enter an access code"
            }

            FeedbackNoteType.PARKING -> {
                title?.text = "Parking Info"
                instructions?.text = "Enter a note regarding parking for this building"
            }

            FeedbackNoteType.TIP -> {
                title?.text = "Tip Info"
                instructions?.text = "Enter a note regarding tips for this building"
            }

            FeedbackNoteType.CRIME -> {
                title?.text = "Crime Info"
                instructions?.text = "Enter a note regarding crime for this building"
            }

            FeedbackNoteType.TEXT -> {
                title?.text = "Add a note"
                instructions?.text = "Notify other drivers of any useful info about this location..."
            }
        }
    }


    override fun setScreenName() {
        screenName = "data_collector_dialog"
    }
}