package ai.beans.common.pojo.search

data class AddressUnitsResponse(val items: ArrayList<UnitListForAddress>) {
    fun hasUnits() : Boolean {
        if(items.isNullOrEmpty())
            return false
        else {
            val unitList = items[0]
            return !unitList!!.units.isNullOrEmpty()
        }
    }
}
