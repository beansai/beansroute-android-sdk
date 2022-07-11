package ai.beans.common.viewmodels

import ai.beans.common.events.ResetStopViews
import ai.beans.common.utils.ObservableData
import ai.beans.common.networking.isp.getPathForRoute
import ai.beans.common.pojo.*
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.LinkedHashMap

class RouteStopsViewModel(application: Application) : AndroidViewModel(application) , LifecycleObserver  {
    var hasNewRoutesData = MutableLiveData<Boolean>()
    var hasNewRoutePath = MutableLiveData<Boolean>()
    var hasNewSelectedStop = MutableLiveData<Boolean>()

    var allStopsIncludingChildren = ArrayList<RouteStop>()

    var allStops = ArrayList<RouteStop>()
    var allStopsHashMap = LinkedHashMap<String, RouteStop>()
    var pathSegments : RoutePath?= null

    private var optimizedRouteStopMap = HashMap<String, Int>()
    private var apartmentMap = HashMap<String, ArrayList<RouteStop>>()
    private var currentActiveStop : RouteStop ?= null
    val mutex = Mutex()

    var gson : Gson = Gson()

    init {
        MainScope().launch {
            initStopsData()
        }
    }

    fun setStops(allStopsIncludingChildren : ArrayList<RouteStop>) {
        this.allStopsIncludingChildren.clear()
        this.allStopsIncludingChildren.addAll(allStopsIncludingChildren)
        MainScope().launch {
            initStopsData()
        }
    }

    private suspend fun initStopsData() {
        MainScope().launch {
            refreshInMemoryStopData()
            refreshPathsForOptimizedRoute()

            MainScope().launch {
                Log.d("SYNC", "Notify Views")
                hasNewRoutesData.value = true
            }
        }
    }

    suspend fun refreshPathsForOptimizedRoute() {
        var fetchOptmizedPaths = MainScope().async(Dispatchers.IO) {
            //We get all the optimized stops from the Db...
            //We check if each stop actually exists as a RouteStop.
            //If not we remove that row from the table...
            //Finally we collect all the optimized stops and request server to give us a path thru them
            optimizedRouteStopMap.clear()
            var listOfStops = ArrayList<RouteStop>()
            listOfStops.addAll(allStops)

            if (listOfStops.isNotEmpty()) {
                var stops = RouteStops()
                stops.item = listOfStops
                val pathResponse = getPathForRoute(stops)
                if (pathResponse.success) {
                    pathSegments = pathResponse.data
                }
            } else {
                pathSegments = null
            }
            MainScope().launch(Dispatchers.Main) {
                hasNewRoutePath.value = true
            }
        }
        fetchOptmizedPaths.await()
    }

    suspend fun getAllStops()  : List<RouteStop> {
        var fullList = ArrayList<RouteStop>()
        fullList.addAll(allStops)
        return fullList

        /*var optimizedStops = optimizedStopsDao.getAllOptimizedStops()
        var stopList = ArrayList<RouteStop>()
        var list : List<RouteStopWithNumbers> ?= null

        var badStops = stopsDao.getBadStops(searchString.value, sidFilterRange.value)
        var finishedStops = stopsDao.getFinishedStops(searchString.value, sidFilterRange.value)
        var newStops = stopsDao.getUnfinishedStops(searchString.value, sidFilterRange.value)
        var fullList = ArrayList<RouteStopWithNumbers>()
        fullList.addAll(badStops)
        fullList.addAll(finishedStops)
        fullList.addAll(newStops)
        if(fullList != null) {
            var index = 1
            for(stopObj in fullList) {
                if(stopObj.routeStop != null) {
                    if (stopObj.optimizedStop != null) {
                        stopObj.routeStop!!.route_display_number =
                            stopObj.optimizedStop!!.display_number
                        stopObj.routeStop!!.in_optimization = true
                    } else {
                        //We set the display number based on certain flags set by the
                        //backend and passed in the Route object
                        if(optimizedStops.isNullOrEmpty()) {
                            var routeInfo = RouteInfoHolder.get()

                            if(routeInfo?.show_default_ordering == true) {
                                if (routeInfo.show_source_seq_number) {
                                    stopObj.routeStop!!.route_display_number =
                                        stopObj.routeStop!!.source_seq
                                } else {
                                    stopObj.routeStop!!.route_display_number = index
                                }
                            }
                        }

                    }
                    stopList.add(stopObj.routeStop!!)
                    index++
                }
            }
        }
        return stopList*/
    }

    suspend fun getTotalStopCount()  : Int {
        return allStops.size
    }

    suspend fun getCompletedStopCount() : Int {
        return allStops.filter { stop -> stop.status == RouteStopStatus.FINISHED || stop.status == RouteStopStatus.FAILED }.size
    }

    fun getStopLabel(stop: RouteStop): Int? {
        return optimizedRouteStopMap[stop.list_item_id]
    }

    fun getChildStopsForParent(parentStopId: String?): ArrayList<RouteStop>? {
        return apartmentMap!!.get(parentStopId)
    }

    fun getCurrentActiveStop(): RouteStop? {
        return currentActiveStop
    }

    fun getCurrentStop(): RouteStop? {
        return currentActiveStop
    }

    fun hasOptimizedRoute() : Boolean {
        return optimizedRouteStopMap.size > 0
    }

    fun getAllChildStops(stopId : String?) : List<RouteStop> {
        return allStopsIncludingChildren.filter { stop -> stop.parent_list_item_id == stopId }
    }

    fun getStopDetails(stopId: String): RouteStop? {
        return allStopsIncludingChildren.filter { stop -> stop.list_item_id == stopId }.getOrNull(0)
    }

    fun setCurrentStop(stop: RouteStop?) {
        if(stop != null) {
            currentActiveStop = stop
        } else {
            currentActiveStop = null
            //currentActiveStopIndex = null
        }
        hasNewSelectedStop.value = true
    }

    fun moveToPreviousStop() {
    }

    fun moveToNextStop() {
        if (currentActiveStop != null ) {
            var displayNum = optimizedRouteStopMap[currentActiveStop!!.list_item_id]
            if (displayNum != null) {
                //We can move ahead only if the current stop is part of the optimized route
                //Ok...we found the stop...but the rules are different for
                //Single stops vs apts
                //For single stops, we just move to the next one in the optimized route.
                var allDone = true
                if (currentActiveStop!!.has_apartments) {
                    //Its an apt stop....check if we have finished all
                    var children = apartmentMap[currentActiveStop!!.list_item_id]
                    if(children != null) {
                        for (aptStop in children) {
                            if (aptStop.status == RouteStopStatus.NEW) {
                                allDone = false
                                break
                            }
                        }
                    }
                }
                if (allDone) {
                    var nextDisplayNum = displayNum + 1
                    for (entry in optimizedRouteStopMap.entries) {
                        if (entry.value == nextDisplayNum) {
                            var stopId = entry.key
                            currentActiveStop = allStopsHashMap[stopId]
                            hasNewSelectedStop.value = true
                            break
                        }
                    }
                }
            } else {
                //We have no logical next stop to go to as this stop is not
                //in an optimized list
                //We close the current stop only if its not an apt stop.
                //Because then they are truly done...
                //If it is an apt stop, we check if all of them are done...
                //If not we stay where we are. If yes, null the active stop
                var allDone = true
                if(currentActiveStop!!.has_apartments) {
                    //Its an apt stop....check if we have finished all
                    var children = apartmentMap[currentActiveStop!!.list_item_id]
                    if(children != null) {
                        for (aptStop in children) {
                            if (aptStop.status == RouteStopStatus.NEW) {
                                allDone = false
                                break
                            }
                        }
                    }
                }
                if(allDone) {
                    currentActiveStop = null
                    //currentActiveStopIndex = null
                    hasNewSelectedStop.value = true
                }
            }
        }
    }

    suspend private fun refreshInMemoryStopData() {
        apartmentMap.clear()
        allStops.clear()
        allStopsHashMap.clear()

        // The assumption here is that the list is ordered, as in every parents is immediately followed
        // by its children. In practice, that does not seem to be the case. (POS ERROR)
        // We'll guard against it.

        var currentIx = 1
        for (task in allStopsIncludingChildren.withIndex()) {
            if (!(task.value.type == RouteStopType.WAREHOUSE_PICKUP || task.value.type == RouteStopType.WAREHOUSE_DROPOFF)) {
                // Assuming parents are at the correct place, they get the correct number.
                // If parents were not the correct place, they would get a strange number. (POS ERROR)
                task.value.route_display_number = currentIx
                if (!task.value.has_apartments) {
                    // If not a parent stop, the stop gets a number.
                    currentIx = currentIx + 1
                }
            }

            // Do nothing more if this is a child
            if (task.value.parent_list_item_id != null && task.value.parent_list_item_id != "") {
                continue
            }
            // Parents and top level items only
            Log.d("PRINT", (if (task.value.has_apartments) (task.value.apartment_count ?: 1) else 1).toString())

            if (!(task.value.type == RouteStopType.WAREHOUSE_PICKUP || task.value.type == RouteStopType.WAREHOUSE_DROPOFF)) {
                allStops.add(task.value)
                allStopsHashMap.put(task.value.list_item_id!!, task.value)
                optimizedRouteStopMap.put(task.value.list_item_id!!, task.index)
            }

            if (task.value.has_apartments) {
                // Parents only
                var childList = getAllChildStops(task.value.list_item_id!!)
                var aptList = ArrayList<RouteStop>()
                for (aptStop in childList) {
                    aptList.add(aptStop)
                }
                if (!aptList.isEmpty()) {
                    apartmentMap.put(task.value.list_item_id!!, aptList)
                }
            }
        }

        for (task in allStopsIncludingChildren.withIndex()) {
            if (task.value.parent_list_item_id != null && task.value.parent_list_item_id != "") {
                continue
            }

            // Parents and top level items only
            if (apartmentMap.containsKey(task.value.list_item_id!!)) {
                // Parents only
                allStopsHashMap[task.value.list_item_id!!]?.children = apartmentMap.get(task.value.list_item_id!!)

                // Fix for POS ERROR
                if (apartmentMap.get(task.value.list_item_id!!) != null) {
                    var min = Int.MAX_VALUE
                    for (childStop in apartmentMap.get(task.value.list_item_id!!)!!) {
                        min = Math.min(min, childStop.route_display_number ?: 0)
                    }
                    task.value.route_display_number = min
                }
            }
        }

        //Refresh the current active stop
        if(currentActiveStop != null) {
            //Check if this stop is still valid
            var stop = allStopsHashMap[currentActiveStop!!.list_item_id]
            if (stop  != null) {
                //We found the stop
                currentActiveStop = stop
            } else {
                //Newly refreshed data does not have the current selected stop.
                //COuld be because we have refreshed to a different route
                MainScope().launch {
                    setCurrentStop(null)
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onActivityCreated() {
        MainScope().launch {
            EventBus.getDefault().post(ResetStopViews())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun registerForEvents() {
        Log.d("SYNC", "Register on Bus")
        EventBus.getDefault().register(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun refresh() {
        Log.d("SYNC", "Back into the app...lets refetch eveything")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun deregisterForEvents() {
        Log.d("SYNC", "UN Register on Bus")
        EventBus.getDefault().unregister(this)
    }
}