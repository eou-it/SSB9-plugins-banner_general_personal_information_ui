package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.person.PersonAddressService
import net.hedtech.banner.general.person.PersonAddressCompositeService
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.AddressTypeService
import net.hedtech.banner.general.system.CountyService
import net.hedtech.banner.general.system.StateService
import net.hedtech.banner.general.system.NationService
import org.codehaus.groovy.grails.web.json.JSONObject

class PersonProfileDetailsController {
    def addressTypeService
    def countyService
    def stateService
    def nationService
    def personAddressService
    def personAddressCompositeService

    private def findPerson() {
        return PersonUtility.getPerson(PersonProfileControllerUtility.getPrincipalPidm())
    }

    def getAddressesForCurrentUser() {
        def model = [:]
        def pidm = PersonProfileControllerUtility.getPrincipalPidm()

        if (pidm) {
            def map = [pidm: pidm]

            try {
                model = personAddressService.getActiveAddresses(map)
            } catch (ApplicationException e) {
                render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
            }
        }

        JSON.use("deep") {
            render model as JSON
        }
    }

    def getCountyList() {
        def map = PersonProfileControllerUtility.getFetchListParams(params)

        try {
            render countyService.fetchCountyList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getStateList() {
        def map = PersonProfileControllerUtility.getFetchListParams(params)

        try {
            render stateService.fetchStateList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getNationList() {
        def map = PersonProfileControllerUtility.getFetchListParams(params)

        try {
            render nationService.fetchNationList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def addAddress() {
        def map = request?.JSON ?: params
        def pidm = PersonProfileControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(map)

        def newAddress = map
        newAddress.pidm = pidm

        try {
            // convert date Strings to Date objects
            if(map.fromDate) {
                newAddress.fromDate = DateUtility.parseDateString(map.fromDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
            }
            if(map.toDate) {
                newAddress.toDate = DateUtility.parseDateString(map.toDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
            }

            // inner entities need to be actual domain objects
            newAddress.addressType = addressTypeService.fetchByCode(newAddress.addressType.code)
            if(newAddress.county)
                newAddress.county = countyService.fetchCounty(newAddress.county.code)
            if(newAddress.state)
                newAddress.state = stateService.fetchState(newAddress.state.code)
            if(newAddress.nation)
                newAddress.nation = nationService.fetchNation(newAddress.nation.code)

            def addresses = []
            addresses[0] = [:]
            addresses[0].personAddress = newAddress
            personAddressCompositeService.createOrUpdate([createPersonAddressTelephones: addresses])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    private def fixJSONObjectForCast(JSONObject json) {
        json.each {entry ->
            // Make JSONObject.NULL a real Java null
            if (entry.value == JSONObject.NULL) {
                entry.value = null

//            If we ever want to fix dates, this is one possible solution
//            } else if (entry.key == "lastModified") {
//                // Make this date string a real Date object
//                entry.value = DateUtility.parseDateString(entry.value, "yyyy-MM-dd'T'HH:mm:ss'Z'")
            }
        }
    }
}
