package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.person.PersonAddressService
import net.hedtech.banner.general.person.PersonUtility

class PersonProfileDetailsController {

    def personAddressService

    private def findPerson() {
        return PersonUtility.getPerson(ControllerUtility.getPrincipalPidm())
    }

    def getAddressesForCurrentUser() {
        def model = [:]
        def pidm = ControllerUtility.getPrincipalPidm()

        if (pidm) {
            def map = [pidm: pidm]

            try {
                model = personAddressService.getActiveAddresses(map)
            } catch (ApplicationException e) {
                render ControllerUtility.returnFailureMessage(e) as JSON
            }
        }

        JSON.use("deep") {
            render model as JSON
        }
    }
}
