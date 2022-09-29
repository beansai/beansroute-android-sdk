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

//        stops.add(RouteStop.simpleInstance(
//            "id1",
//            "56 JALAN 4/48,TAMAN DATO SENU,51000,SENTUL,KUALA LUMPUR,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id101",
//            "56 JALAN 4/48,TAMAN DATO SENU,51000,SENTUL,KUALA LUMPUR,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id2",
//            "NO. 28, JALAN DATO SENU 32/48TAMAN DATO SENU, 5100,0 KUALA LUMPUR, Kuala Lumpur, Malaysia,51000,KOTA BHARU,Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id3",
//            "NO 23 JALAN SENTUL JAYA 1 ,TAMAN SENTUL BAHAGIA SENTUL,51100,KUALA LUMPUR,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id4",
//            "No. 3, Jalan Dato Senu 5,Taman Dato Senu,51000,Sentul,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id5",
//            "No 47, Jalan Dato Senu 4,,Sentul,,51000,Kuala Lumpur,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id6",
//            "No. 50 Jalan 12 Taman Dato Senu,51000,Sentul,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id7",
//            "NO 21A JALAN DATO SENU 12,TAMAN DATO SENU,51000,Sentul,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id8",
//            "no 31 jalan 13 Taman Datuk Senu, Sentul, 51000 Kua,la Lumpur, Kuala Lumpur, Malaysia,51000,Shah Alam,Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id9",
//            "A-06-15 KONDOMINIUM SENTUL UTAMA JALAN DATO SENU 2,6,TAMAN DATO SENU KUALA LUMPUR.,51000,Sentul,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id10",
//            "SMK Convent Sentul, Jalan Sentul,,51000,Sentul,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))
//        stops.add(RouteStop.simpleInstance(
//            "id11",
//            "22 Jalan 1/48F Off Lorong Sentul Bahagia 4,51100,Sentul,Wilayah Persekutuan Kuala Lumpur,MY",
//            "",
//            RouteStopType.DROPOFF,
//            RouteStopStatus.NEW
//        ))

        stops.add(RouteStop.simpleInstance(
            "8728",
            "D1608 casa Tiara Apartment jalan kemajuan ss16, 47500, Subang Jaya, Selangor,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8710",
            "LEVEL 5,AEON BIG SUBANG JAYA NO 3,JALAN SS16/1,.,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8711",
            "NO.40, JALAN SS18/3C, 47500 SUBANG JAYA, Selangor, Malaysia,47500,Iskandar Puteri,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8712",
            "IQOS AUTHORISED CENTRE, No 15 (Ground Floor), Jalan SS15/8B 47500 Subang Jaya, Selangor, Malaysia,47500,Kuala Lumpur,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8713",
            "18   ,   Jalan ss17/1D,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8714",
            "LEVEL 5 AEON BIG SUBANG JAYA NO 3 SS 16/1 SUBANG JAYA,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8715",
            "64, Jln SS18/4G, 47500, Subang Jaya, Selangor,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8716",
            "Level 1 East Wing Wisma Consplant 2 No 7 Jalan SS16/1,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8717",
            "29G   ,   Medical Specialist Clinic Subang,Jalan SS 15/5a, Ss 15,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8718",
            "No 118  Lafite Apartment SS17/1G,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8719",
            "No. 1, 1st Floor, Jalan SS15/8A, 47500 Subang Jaya, Selangor,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8720",
            "No 64 , DIRECTD SUBANG JAYA. Jalan Ss15/4b subang jaya Selangor Malaysia ,47500,Ampang,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8721",
            "4, Jalan SS 17/2B,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8722",
            "25 Jalan SS18/4E,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8723",
            "No.29, Jalan SS17/2C, Subang Jaya, 47500 SELANGOR,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8724",
            "no 72 jalan SS18/2A, 47500 subang jaya  Selangor Malaysia ,47500,BANDAR PUNCAK ALAM,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8743",
            "345   ,   #345, Subang Jaya Lafite Apartment, Jln SS 17/1G, Ss 17,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8725",
            "LEVEL 5 AEON BIG SUBANG JAYA,No 3 Jalan SS16/1,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8727",
            "Level 5, AEON BIG Subang Jaya,No 3, Jalan SS 16/1,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8729",
            "17 GALLERY,21,JALAN SS15/8B,47500,SUBANG JAYA,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8730",
            "3, Jalan SS 19/4A, 47500, Subang Jaya, Selangor,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8731",
            "32, jalan ss18/5f,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8732",
            "LEVEL 5, AEON BIG SUBANG JAYA,NO.3 JALAN SS16/1,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8733",
            "No. 37, Jalan SS18/4A, SS18, Subang Jaya,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8734",
            "Lvl 5 Aeon Big Subang Jaya Jalan Kemajuan Subang,Ss16,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8709",
            "NINJA VAN   Level 14 , west wing, Wisma consplant 1, jalan ss16/4,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8735",
            "Lvl 5 Aeon Big Subang Jaya Jalan Kemajuan Subang,Ss16,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8737",
            "11, Jalan SS18/6, string,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8738",
            "No 112, Jalan SS18/1 Subang Jaya Selangor Malaysia ,47500,SERI KEMBANGAN,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8739",
            "level 5 aeon big subang jaya,Jln Ss 16/1,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8740",
            "LEVEL 5, AEON BIG SUBANG JAYA ,NO 3, JALAN SS16/1 SUBANG JAYA ,,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8741",
            "LEVEL 5 AEON BIG SUBANG JAYA, NO 3 JALAN SS16/1,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8742",
            "97 Jalan SS18/1B, 47500 Subang Jaya, Selangor.,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8736",
            "Lvl 5 Aeon Big Subang Jaya Jalan Kemajuan Subang,Ss16,47500,Subang Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))
        stops.add(RouteStop.simpleInstance(
            "8726",
            "45 Jalan SS18/2B,47500,Petaling Jaya,Selangor,MY",
            "",
            RouteStopType.DROPOFF,
            RouteStopStatus.NEW
        ))

//        MainScope().launch {
//            stops.forEach {
//                try {
//                    val response = BeansEnterpriseNetworkService.BEANS_ENTERPRISE_API!!.getSearchResponse(it.address!!, it.unit, GeoPoint(0.0, 0.0))
//                    if (response != null && response.code() == 200) {
//                        it.display_position = response.body()!!.data!!.getNavigationPoint()
//                        it.position = response.body()!!.data!!.getNavigationPoint()
//                        if (response.body()!!.data!!.routes != null
//                            && response.body()!!.data!!.routes!!.size > 0
//                            && response.body()!!.data!!.routes!!.get(0).destination?.country_iso3 != "MYS") {
//                            it.status = RouteStopStatus.NOLOCATION
//                            // Optional:
//                            // it.display_position = null
//                            // it.position = null
//                        } else {
//                            if (it.display_position == null || it.position == null) {
//                                it.status = RouteStopStatus.NOLOCATION
//                                it.route_display_number = 5;
//                            } else {
//                                it.route_display_number = 5;
//                                it.status = RouteStopStatus.NEW
//                            }
//                        }
//                    } else {
//                        var managedResponse = ApiResponse.handleResponse(response, response.body())
//                        it.status = RouteStopStatus.NOLOCATION
//                    }
//                } catch (ex: IOException) {
//                    var managedResponse = ApiResponse.handleResponse(null, Envelope<SearchResponse>())
//                    it.status = RouteStopStatus.NOLOCATION
//                }
//            }
//
//            stops = groupStops(stops)
//
//            var parentStops = ArrayList<RouteStop>()
//            stops.forEach {
//                if (it.address != "" && it.position != null && (it.parent_list_item_id == null || it.parent_list_item_id == "")) {
//                    parentStops.add(it)
//                }
//            }
//            var optimizedParentStops = optimizeStopList(OptimizeStopRequest(parentStops), GeoPoint(2.9244089,101.4580797), GeoPoint(3.168636,101.614877))
//            var orderedStops = ArrayList<RouteStop>()
//            for (routeStop in optimizedParentStops.data!!.item!!) {
//                stops.forEach {
//                    if (it.list_item_id == routeStop.list_item_id) {
//                        orderedStops.add(it)
//                    }
//                }
//                stops.forEach {
//                    if (it.parent_list_item_id != null && it.parent_list_item_id != "" && it.parent_list_item_id == routeStop.list_item_id) {
//                        orderedStops.add(it)
//                    }
//                }
//            }
//
//            stops = orderedStops
//    }

        MainScope().launch {
            var optimizedParentStops = optimizeStopList(OptimizeStopRequest(stops), GeoPoint(2.9244187,101.4580727), GeoPoint(3.168636,101.614877))
            stops = optimizedParentStops.data!!.item!!
            stops.forEach {
                if (it.status == RouteStopStatus.NOLOCATION) {
                    it.display_position = GeoPoint(0.0, 0.0)
                    it.position = GeoPoint(0.0, 0.0)
                }
                if (it.children != null && !it.children!!.isEmpty()) {
                    it.has_apartments = true
                    it.apartment_count = it.children!!.size
                }
            }
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