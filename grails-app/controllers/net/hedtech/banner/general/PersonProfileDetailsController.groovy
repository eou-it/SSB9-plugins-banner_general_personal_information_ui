package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonAddressService
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.system.StateService

class PersonProfileDetailsController {
    def stateService
    def personAddressService

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

    def getStateList() {
        def maxItems = params.int('max')
        def map = [
            max: maxItems,
            offset: params.int('offset') * maxItems,  // Convert the page-level offset passed as a param to an item-level offset
            searchString: params.searchString
        ]

        try {
            render stateService.fetchStateList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render ControllerUtility.returnFailureMessage(e) as JSON
        }
    }
}
