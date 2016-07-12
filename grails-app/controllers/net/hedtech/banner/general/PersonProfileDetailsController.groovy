package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonAddressService
import net.hedtech.banner.general.person.PersonAddressUtility
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.CountyService
import net.hedtech.banner.general.system.StateService
import net.hedtech.banner.general.system.NationService

class PersonProfileDetailsController {
    def countyService
    def stateService
    def nationService
    def personAddressService

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
                personAddress.addressType = it.addressType?.description
                personAddress.fromDate = it.fromDate
                personAddress.toDate = it.toDate
                personAddress.address = PersonAddressUtility.formatDefaultAddress(
                        [houseNumber:it.houseNumber,
                         streetLine1:it.streetLine1,
                         streetLine2:it.streetLine2,
                         streetLine3:it.streetLine3,
                         streetLine4:it.streetLine4,
                         city:it.city,
                         state:it.state?.description,
                         zip:it.zip,
                         county:it.county,
                         country:it.nation])

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
}
