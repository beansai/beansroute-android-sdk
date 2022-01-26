package ai.beans.tester.ui

import ai.beans.common.application.BeansApplication
import ai.beans.common.networking.isp.optimizeStopList
import ai.beans.common.pojo.*
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.viewmodels.RouteStopsViewModel
import ai.beans.common.widgets.camera.CameraCaptureResults
import ai.beans.tester.R
import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TestFragment : BeansFragment() {
    var routeDataViewModel: RouteStopsViewModel? = null
    var stops = ArrayList<RouteStop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routeDataViewModel = ViewModelProviders.of(
            activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansApplication.getInstance()!!)
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
            GeoPoint(37.372592119124505, -122.06539526711047),
            GeoPoint(37.372592119124505, -122.06539526711047),
            BeansAddressComponents(
                "Mountain View",
                "CA",
                "94040",
                "USA",
                "1200 Dale Ave"
            ),
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
            GeoPoint(37.324716785269935, -122.02028896011667),
            GeoPoint(37.324716785269935, -122.02028896011667),
            BeansAddressComponents(
                "Cupertino",
                "CA",
                "95014",
                "USA",
                "10136 McLaren Pl"
            ),
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
            GeoPoint(37.40734821753622, -122.10791942762405),
            GeoPoint(37.40734821753622, -122.10791942762405),
            BeansAddressComponents(
                "Mountain View",
                "CA",
                "94040",
                "USA",
                "2255 Showers Dr"
            ),
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
            GeoPoint(37.40734821753622, -122.10791942762405),
            GeoPoint(37.40734821753622, -122.10791942762405),
            BeansAddressComponents(
                "Mountain View",
                "CA",
                "94040",
                "USA",
                "2255 Showers Dr"
            ),
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
            GeoPoint(37.40734821753622, -122.10791942762405),
            GeoPoint(37.40734821753622, -122.10791942762405),
            BeansAddressComponents(
                "Mountain View",
                "CA",
                "94040",
                "USA",
                "2255 Showers Dr"
            ),
            "0004",
            "Notes for test4",
            null,
            false,
            0,
            0
        ))
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

        routeDataViewModel?.setStops(stops)
        MainScope().launch {
            var parentStops = ArrayList<RouteStop>()
            stops.forEach {
                if (it.parent_list_item_id == null || it.parent_list_item_id == "") {
                    parentStops.add(it)
                }
            }
            var optimizedParentStops = optimizeStopList(OptimizeStopRequest(parentStops), GeoPoint(37.62708995575689, -122.36044460693604), GeoPoint(37.0, -122.0), false)
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
            routeDataViewModel?.setStops(orderedStops)
        }

        var fragment = childFragmentManager?.findFragmentById(R.id.embedded_map_fragment)
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
}