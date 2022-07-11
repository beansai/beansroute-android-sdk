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
import ai.beans.common.utils.groupStops
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

        stops.add(RouteStop.simpleInstance(
            "id1",
            "Lot 857 i Jl. Lorong masjid 1. Kg. Sg. Kayu Ara Damansara Utama Petaling Jaya,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id2",
            "Shell Station, 1 Jalan Kenanga PJU6A Vista Damansara (Kg Kayu Ara),47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id3",
            "20, Jalan Teratai, PJU 6, 47400 Petaling Jaya, Selangor, Malaysia,47400,Sungai Petani,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id4",
            "18-3A, Glomac Centro,Jln Teratai PJU 6A,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id5",
            "G-16-8, Pangsapuri Pelangi Ara, Pju 6A Jalan Teratai, Petaling Jaya Selangor,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id6",
            "G11 Sri Araville, Kayu Ara, Jalan Dahlia, PJU 6A, PETALING JAYA Selangor.,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id7",
            "LEE ZI TIAN B-8-6 PELANGI UTAMA JALAN MASJID PJU 6A 47400 Petaling Jaya Selangor,47400,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id8",
            "38 ,jalan 3 kampung kayu ara indah, string,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id9",
            "19-09, Glomac Centro Serviced Apartment, Jalan Teratai PJU 6A,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id10",
            "1007i, Jalan Kekwa, Kampung kayu Ara,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id11",
            "G-5-18 PANGSAPURI PELANGI ARA PJU6A JALAN TERATAI KAMPUNG SUNGAI KAYU ARA,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id12",
            "Glomac Centro, Lorong Masjid 1, Kampung Sungai Kayu Ara 1-19, 47400, Petaling Jaya, Selangor,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id13",
            "Lot 866 lorong masjid 2 kg sg kayu ara damansara,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id14",
            "NO32A,JALAN DAHLIA 3,PJU6A,TAMAN MAS UTAMA, 47400, Petaling Jaya, Selangor,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id15",
            "B9-15, Boulevard Residence, Jalan Kenanga,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id16",
            "E-10-3A Pelangi Utama, Jalan Masjid PJU 6A, Petaling Jaya,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id17",
            "GLAMPOT SDN BHD, B-5-26 Block Bougainvillea, 10 Boulevard, Jalan Kenanga, 47400 Kayu Ara,Damansara , Pj, Selangor, Malaysia,47400,Gombak Setia .,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id18",
            "SITI NORHAYATI MAHMOD LOT 1028-T JALAN CEMPAKA KAMPUNG SUNGAI KAYU ARA DAMANSARA JAYA 47400 Petaling Jaya Selangor,47400,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id19",
            "10A-LOT1047, Jalan Cempaka Kampung Sungai Kayu Ara Selangor Malaysia ,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id20",
            "C-5-41   ,   Blok Camilia, 10 Boulevard, SPRINT Expressway, Kampung Sungai Kayu Ara,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "id21",
            "no  10  , d  residency  terrace,  jalan  anggerik,.   kampung sungai   kayu ara 47400 pj selangor,47400,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))

        MainScope().launch {
            stops.forEach {
                try {
                    val response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.getSearchResponse(it.address!!, it.unit, GeoPoint(0.0, 0.0))
                    if (response != null && response.code() == 200) {
                        it.display_position = response.body()!!.data!!.getNavigationPoint()
                        it.position = response.body()!!.data!!.getNavigationPoint()
                        if (response.body()!!.data!!.routes != null
                            && response.body()!!.data!!.routes!!.size > 0
                            && response.body()!!.data!!.routes!!.get(0).destination?.country_iso3 != "MYS") {
                            it.status = RouteStopStatus.NOLOCATION
                            // Optional:
                            // it.display_position = null
                            // it.position = null
                        } else {
                            if (it.display_position == null || it.position == null) {
                                it.status = RouteStopStatus.NOLOCATION
                                it.route_display_number = 5;
                            } else {
                                it.route_display_number = 5;
                                it.status = RouteStopStatus.NEW
                            }
                        }
                    } else {
                        var managedResponse = ApiResponse.handleResponse(response, response.body())
                        it.status = RouteStopStatus.NOLOCATION
                    }
                } catch (ex: IOException) {
                    var managedResponse = ApiResponse.handleResponse(null, Envelope<SearchResponse>())
                    it.status = RouteStopStatus.NOLOCATION
                }
            }

            stops = groupStops(stops)
            // stops = groupStops(stops)

            var parentStops = ArrayList<RouteStop>()
            stops.forEach {
                if (it.address != "" && it.position != null && (it.parent_list_item_id == null || it.parent_list_item_id == "")) {
                    parentStops.add(it)
                }
            }
            var optimizedParentStops = optimizeStopList(OptimizeStopRequest(parentStops), GeoPoint(2.9244089,101.4580797), GeoPoint(3.168636,101.614877))
            var orderedStops = ArrayList<RouteStop>()
            for (routeStop in optimizedParentStops.data!!.item!!) {
                stops.forEach {
                    if (it.list_item_id == routeStop.list_item_id) {
                        orderedStops.add(it)
                    }
                }
                stops.forEach {
                    if (it.parent_list_item_id != null && it.parent_list_item_id != "" && it.parent_list_item_id == routeStop.list_item_id) {
                        orderedStops.add(it)
                    }
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