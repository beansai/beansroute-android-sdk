package ai.beans.tester.ui

import ai.beans.common.application.BeansApplication
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

class TestFragment : BeansFragment() {
    var routeDataViewModel: RouteStopsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routeDataViewModel = ViewModelProviders.of(
            activity!!,
            ViewModelProvider.AndroidViewModelFactory(BeansApplication.getInstance()!!)
        ).get(RouteStopsViewModel::class.java)
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

        var fragment = childFragmentManager?.findFragmentById(R.id.embedded_map_fragment)
        var stops = ArrayList<RouteStop>()
        stops.add(RouteStop(
            "id1",
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
            "F1",
            "Test1",
            "",
            0,
            0,
            0,
            GeoPoint(37.329742, -122.029042),
            GeoPoint(37.329742, -122.029042),
            BeansAddressComponents(
                "Mountain View",
                "CA",
                "94040",
                "USA",
                "2255 Showers Dr"
            ),
            "0001",
            "Notes for test1",
            null,
            true,
            2,
            0
        ))
        stops.add(RouteStop(
            "id2",
            "id1",
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
            GeoPoint(37.332582739071384, -122.0258231141054),
            GeoPoint(37.332582739071384, -122.0258231141054),
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
            false,
            0,
            0
        ))
        stops.add(RouteStop(
            "id3",
            "id1",
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
            GeoPoint(37.324510008439994, -122.03555981793365),
            GeoPoint(37.324510008439994, -122.03555981793365),
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
            "F4",
            "Test4",
            "",
            0,
            0,
            3,
            GeoPoint(37.324610008439994, -122.03555981793365),
            GeoPoint(37.324610008439994, -122.03555981793365),
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
        routeDataViewModel?.setStops(stops)
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

    override fun setTitle() {
        "Hello Again!"
    }

}