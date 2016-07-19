package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.person.PersonAddressService
import net.hedtech.banner.general.person.PersonAddressCompositeService
import net.hedtech.banner.general.person.PersonAddressUtility
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.overall.AddressRolePrivilegesCompositeService
import net.hedtech.banner.general.system.CountyService
import net.hedtech.banner.general.system.StateService
import net.hedtech.banner.general.system.NationService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

class PersonProfileDetailsController {
    def addressRolePrivilegesCompositeService
    def countyService
    def stateService
    def nationService
    def personAddressService
    def personAddressCompositeService

    private def findPerson() {
        return PersonUtility.getPerson(PersonProfileControllerUtility.getPrincipalPidm())
    }

    def getActiveAddressesForCurrentUser() {
        def model = [:]
        def pidm = PersonProfileControllerUtility.getPrincipalPidm()

        if (pidm) {
            def map = [pidm: pidm]
            def addresses

            try {
                addresses = personAddressService.getActiveAddresses(map).list
            } catch (ApplicationException e) {
                render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
            }

            // TODO: if masking turns out to be needed here, search for "maskingRule" in EmployeeProfileController.groovy
            // TODO: in Employee Profile app.
            model.addresses = []

            def personAddress

            addresses.each { it ->
                personAddress = [:]
                personAddress.id = it.id
                personAddress.version = it.version
                personAddress.addressType = it.addressType
                personAddress.fromDate = it.fromDate
                personAddress.toDate = it.toDate
                personAddress.houseNumber = it.houseNumber
                personAddress.streetLine1 = it.streetLine1
                personAddress.streetLine2 = it.streetLine2
                personAddress.streetLine3 = it.streetLine3
                personAddress.streetLine4 = it.streetLine4
                personAddress.city = it.city
                personAddress.county = it.county
                personAddress.state = it.state
                personAddress.zip = it.zip
                personAddress.nation = it.nation
                personAddress.displayAddress = PersonAddressUtility.formatDefaultAddress(
                        [houseNumber:it.houseNumber,
                         streetLine1:it.streetLine1,
                         streetLine2:it.streetLine2,
                         streetLine3:it.streetLine3,
                         streetLine4:it.streetLine4,
                         city:it.city,
                         state:it.state?.description,
                         zip:it.zip,
                         county:it.county?.description,
                         country:it.nation?.nation])

                model.addresses << personAddress
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

    def getAddressTypeList() {
        def map = PersonProfileControllerUtility.getFetchListParams(params)

        try {
            render addressRolePrivilegesCompositeService.fetchUpdateableAddressTypeList(getRoles(), map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def addAddress() {
        def newAddress = request?.JSON ?: params
        newAddress.pidm = PersonProfileControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(newAddress)

        try {
            personAddressService.checkAddressFieldsValid(newAddress)

            // convert date Strings to Date objects
            newAddress.fromDate = DateUtility.parseDateString(newAddress.fromDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
            if(newAddress.toDate)
                newAddress.toDate = DateUtility.parseDateString(newAddress.toDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")

            // inner entities need to be actual domain objects
            newAddress.addressType = addressRolePrivilegesCompositeService.fetchAddressType(getRoles(), newAddress.addressType.code)
            if(newAddress.county?.code)
                newAddress.county = countyService.fetchCounty(newAddress.county.code)
            if(newAddress.state?.code)
                newAddress.state = stateService.fetchState(newAddress.state.code)
            if(newAddress.nation?.code)
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

    def updateAddress() {
        def updatedAddress = request?.JSON ?: params
        updatedAddress.pidm = PersonProfileControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(updatedAddress)

        // Address type is a primary key field and cannot be modified
        updatedAddress.remove("addressType")

        try {
            personAddressService.checkAddressFieldsValid(updatedAddress)

            // Convert date Strings to Date objects
            // TODO: if there are changes to the way dates are handled in addAddress above, similar changes will likely be needed here
            updatedAddress.fromDate = DateUtility.parseDateString(updatedAddress.fromDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
            if(updatedAddress.toDate)
                updatedAddress.toDate = DateUtility.parseDateString(updatedAddress.toDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")

            personAddressService.update(updatedAddress)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonProfileControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteAddresses() {
        def map = request?.JSON ?: params

        try {
            def model = [:]
            def result = personAddressService.delete(map)

            render result as JSON

        } catch (ApplicationException e) {
            def arrayResult = [];
            arrayResult[0] = ControllerUtility.returnFailureMessage(e)

            render arrayResult as JSON
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

    private def getRoles() {
        def roles = SecurityContextHolder?.context?.authentication?.principal?.authorities.collect {
            it.getAssignedSelfServiceRole()
        }
        return roles
    }
}
