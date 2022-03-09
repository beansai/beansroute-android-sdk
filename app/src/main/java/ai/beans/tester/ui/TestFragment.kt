package ai.beans.tester.ui

import ai.beans.common.MapMoved
import ai.beans.common.application.BeansContextContainer
import ai.beans.common.events.PinMoved
import ai.beans.common.events.UpdateStopStatus
import ai.beans.common.networking.ApiResponse
import ai.beans.common.networking.Envelope
import ai.beans.common.networking.isp.BeansEnterpriseNetworkService
import ai.beans.common.networking.isp.optimizeStopList
import ai.beans.common.pojo.*
import ai.beans.common.pojo.search.SearchResponse
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.viewmodels.RouteStopsViewModel
import ai.beans.tester.R
import ai.beans.tester.TestApplication
import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException

class TestFragment : BeansFragment() {
    var routeDataViewModel: RouteStopsViewModel? = null
    var stops = ArrayList<RouteStop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BeansContextContainer.application = TestApplication.getInstance()
        BeansContextContainer.context = TestApplication.getContext()

        routeDataViewModel = ViewModelProviders.of(
            activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansContextContainer.application!!)
        ).get(RouteStopsViewModel::class.java)

        stops.add(RouteStop(
            "id5",
            "",
            "",
            "1200 Dale Ave, Mountain View, CA",
            "43",
            "1200 Dale Ave, Mountain View, CA",
            RouteStopStatus.NEW,
            0L,
            0L,
            0L,
            0L,
            "",
            0L,
            "",
            RouteStopType.DROPOFF,
            "F5",
            "Test5",
            "",
            0,
            0,
            0,
            null,
            null,
            null,
            "0005",
            "Notes for test5",
            null,
            false,
            0,
            0
        ))
        stops.add(RouteStop(
            "id1",
            "",
            "",
            "10136 McLaren Pl, Cupertino, CA",
            "",
            "10136 McLaren Pl, Cupertino, CA",
            RouteStopStatus.NEW,
            0L,
            0L,
            0L,
            0L,
            "",
            0L,
            "",
            RouteStopType.DROPOFF,
            "F1",
            "Test1",
            "",
            0,
            0,
            0,
            null,
            null,
            null,
            "0001",
            "Notes for test1",
            null,
            false,
            0,
            0
        ))
        stops.add(RouteStop(
            "id2",
            "",
            "",
            "2255 Showers Dr, Mountain View, CA",
            "",
            "2255 Showers Dr, Mountain View, CA",
            RouteStopStatus.NEW,
            0L,
            0L,
            0L,
            0L,
            "",
            0L,
            "",
            RouteStopType.DROPOFF,
            "F2",
            "Test2",
            "",
            0,
            0,
            2,
            null,
            null,
            null,
            "0002",
            "Notes for test2",
            null,
            true,
            2,
            0
        ))
        stops.add(RouteStop(
            "id3",
            "id2",
            "",
            "2255 Showers Dr, Mountain View, CA",
            "",
            "2255 Showers Dr, Mountain View, CA",
            RouteStopStatus.NEW,
            0L,
            0L,
            0L,
            0L,
            "",
            0L,
            "",
            RouteStopType.DROPOFF,
            "F3",
            "Test3",
            "",
            0,
            0,
            3,
            null,
            null,
            null,
            "0003",
            "Notes for test3",
            null,
            false,
            0,
            0
        ))
        stops.add(RouteStop(
            "id4",
            "id2",
            "",
            "2255 Showers Dr, Mountain View, CA",
            "",
            "2255 Showers Dr, Mountain View, CA",
            RouteStopStatus.NEW,
            0L,
            0L,
            0L,
            0L,
            "",
            0L,
            "",
            RouteStopType.DROPOFF,
            "F4",
            "Test4",
            "",
            0,
            0,
            3,
            null,
            null,
            null,
            "0004",
            "Notes for test4",
            null,
            false,
            0,
            0
        ))

        MainScope().launch {
            stops.forEach {
                try {
                    val response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.getSearchResponse(it.address!!, it.unit, GeoPoint(0.0, 0.0))
                    if (response != null && response.code() == 200) {
                        it.display_position = response.body()!!.data!!.getNavigationPoint()
                        it.position = response.body()!!.data!!.getNavigationPoint()
                    } else {
                        var managedResponse = ApiResponse.handleResponse(response, response.body())
                        it.status = RouteStopStatus.NOLOCATION
                    }
                } catch (ex: IOException) {
                    var managedResponse = ApiResponse.handleResponse(null, Envelope<SearchResponse>())
                    it.status = RouteStopStatus.NOLOCATION
                }
            }

            var parentStops = ArrayList<RouteStop>()
            stops.forEach {
                if (it.parent_list_item_id == null || it.parent_list_item_id == "") {
                    parentStops.add(it)
                }
            }
            var optimizedParentStops = optimizeStopList(OptimizeStopRequest(parentStops), GeoPoint(37.62708995575689, -122.36044460693604), GeoPoint(37.0, -122.0))
            var orderedStops = ArrayList<RouteStop>()
            for (routeStop in optimizedParentStops.data!!.item!!) {
                stops.forEach {
                    if (it.list_item_id == routeStop.list_item_id) {
                        orderedStops.add(it)
                    }
                }
            }
            stops.forEach {
                if (it.parent_list_item_id != null && it.parent_list_item_id != "") {
                    orderedStops.add(it)
                }
            }

            stops = orderedStops
            routeDataViewModel?.setStops(stops)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_test, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!permmisionManager?.isPermissionEverRequested(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (!permmisionManager?.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permmisionManager?.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun setScreenName() {
        screenName = "TestFragment"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UpdateStopStatus) {
        print (event.stopId)
        // Bring up AGMO POD screen here
        // Based on fail/ success, call the code below (which will now be linked to the buttons on AGMO's POD screen)
        stops.forEach {
            if (it.list_item_id == event.stopId) {
                // Do one of these:
                it.status = RouteStopStatus.FINISHED
                it.status = RouteStopStatus.FAILED
            }
        }
        routeDataViewModel?.setStops(stops)
        routeDataViewModel?.hasNewRoutesData?.value = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: PinMoved) {
        print(event.stopId)
    }
}