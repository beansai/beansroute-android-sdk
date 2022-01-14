package ai.beans.common.widgets.stops

import ai.beans.common.R
import ai.beans.common.maps.BeansMapFragmentImpl
import ai.beans.common.pojo.RouteStop
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ChildStopAdapter(context: Context?) : RecyclerView.Adapter<ChildStopViewHolder>(),
    ActionButtonListener {

    var recyclerView: RecyclerView?= null
    var inflater: LayoutInflater? = null
    var context : Context?= null
    var routeStops: ArrayList<RouteStop> ?= null
    var ownerFragmentBeans: BeansMapFragmentImpl?= null
    var actionButtonListener : ActionButtonListener ?= null

    init {
        this.context = context
        inflater = LayoutInflater.from(context)
    }

    fun attachAdapterToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: ChildStopViewHolder, position: Int) {
        routeStops?.let {
            var stop = routeStops!![position]
            holder.stop = stop
            holder.render()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildStopViewHolder {
        val v = inflater?.inflate(R.layout.child_stop_list_item, parent, false)
        var viewHolder = ChildStopViewHolder(v!!)
        viewHolder.setActionListener(actionButtonListener)
        return viewHolder
    }

    override fun getItemCount(): Int {
        if(routeStops != null) {
            return routeStops!!.size
        } else {
            return 0
        }
    }

    fun setData(routes: ArrayList<RouteStop>) {
        this.routeStops = routes
    }



    override fun onNavigateClicked(currentStop: RouteStop?) {
        if(currentStop != null && ownerFragmentBeans != null) {
            val location =
                "google.navigation:q=" + currentStop!!.position!!.latitudeAsString() + "," + currentStop!!.position!!.longitudeAsString()
            val gmmIntentUri = Uri.parse(location)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(ownerFragmentBeans!!.context!!.packageManager) != null) {
                ownerFragmentBeans!!.startActivity(mapIntent)
            }
        }
    }

    override fun onDeliveredClicked(currentStop: RouteStop?) {
        /*XXif(currentStop?.status!! == RouteStopStatus.FINISHED) {
            ownerFragment?.updateStopStatus(currentStop, RouteStopStatus.NEW)
        }
        else {
            ownerFragment?.updateStopStatus(currentStop, RouteStopStatus.FINISHED)
        }*/
    }

    override fun onAttemptedClicked(currentStop: RouteStop?) {
        /*XXif(currentStop?.status!! == RouteStopStatus.FAILED) {
            ownerFragment?.updateStopStatus(currentStop, RouteStopStatus.NEW)
        }
        else {
            ownerFragment?.updateStopStatus(currentStop, RouteStopStatus.FAILED)
        }*/
    }

    override fun onPrevStopClicked(currentStop: RouteStop?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNextStopClicked(currentStop: RouteStop?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
