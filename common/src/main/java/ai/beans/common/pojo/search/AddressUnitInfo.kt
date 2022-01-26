package ai.beans.common.pojo.search

class AddressUnitInfo() {
    var unit : String ?= null
    var address: String ?= null
    var sectionLabel: String ?= null

    constructor(unitInfo: AddressUnitInfo) : this() {
        unit = unitInfo.unit
        address= unitInfo.address
        sectionLabel = unitInfo.sectionLabel
    }
}
