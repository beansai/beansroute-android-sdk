package ai.beans.common.custom_markers

import ai.beans.common.R
import ai.beans.common.pojo.IconItem
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.row_marker_info.view.*

class MarkerAdapter(val items : ArrayList<IconItem>, val context: Context, val mItemClickListener:ItemClickListener) : RecyclerView.Adapter<MarkerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_marker_info, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.markerType.setText(items.get(position).title?.capitalize())
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .signature(ObjectKey(items?.get(position)))
        holder.markerIv.setupMarkerListIcon(items.get(position))
    }
    override fun getItemCount(): Int {
        return items.size
    }
    interface ItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val markerType = view.markerTypeTV
        val markerIv=view.markerIV
        init {
            view.setOnClickListener {
                mItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

}