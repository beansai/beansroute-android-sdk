package ai.beans.common.pojo

//@Entity(foreignKeys = arrayOf(ForeignKey(entity = RouteStopsTableRow::class,
//    parentColumns = arrayOf("list_item_id"),
//    childColumns = arrayOf("parent_id"),
//    onDelete = ForeignKey.CASCADE)))

class RouteStop(
    var list_item_id: String,

    var parent_list_item_id: String? = null,

    var account_buid: String? = null,

    var address: String? = null,

    var unit: String? = null,

    var formatted_address: String? = null,

    var status: RouteStopStatus? = null,

    var created_at: Long? = null,

    var updated_at: Long? = null,

    var status_updated_at: Long? = null,

    var deliver_by: Long? = null,

    var deliver_by_str: String? = null,

    var deliver_from: Long? = null,

    var deliver_from_str: String? = null,

    var type: RouteStopType? = null,

    var placement: String? = null,

    var customer_name: String? = null,

    var customer_phone: String? = null,

    var route_priority: Int = 0,

    var source_seq: Int = 0,

    var num_packages: Int? = null,

    var position: GeoPoint? = null,

    var display_position: GeoPoint? = null,

    var address_components: BeansAddressComponents? = null,

    var tracking_id: String? = null,

    var notes: String? = null,

    var polygon: BuildingShape? = null,

    var has_apartments: Boolean = false,

    var apartment_count: Int? = null,

    var total_package_count: Int? = null) {

    var route_display_number : Int ?= null

    var children : ArrayList<RouteStop> ?= null

    fun getCopy(): RouteStop {
        //Copy constructor to create a route obj.
        //Used ONLY while POSTING to server on status change
        //Not deep enough to serve as "real" copy constructor
        var copy = RouteStop(list_item_id)
        copy.position = position
        copy.list_item_id = list_item_id
        copy.address = address
        copy.unit = unit
        copy.status = status
        copy.deliver_by = deliver_by
        copy.deliver_from = deliver_from
        copy.notes = notes
        copy.tracking_id = tracking_id
        copy.position = position
        copy.route_priority = route_priority
        copy.parent_list_item_id = parent_list_item_id
        copy.placement = placement
        return copy
    }

}

