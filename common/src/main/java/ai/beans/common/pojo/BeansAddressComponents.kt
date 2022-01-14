package ai.beans.common.pojo

class BeansAddressComponents {
    constructor() {
    }

    constructor(city: String, state: String, zipcode: String, country: String, street: String) {
        this.city = city
        this.state = state
        this.zipcode = zipcode
        this.country = country
        this.street = street
    }

    var city: String? = null
    var state: String? = null
    var zipcode: String? = null
    var country: String? = null
    var street: String? = null
}