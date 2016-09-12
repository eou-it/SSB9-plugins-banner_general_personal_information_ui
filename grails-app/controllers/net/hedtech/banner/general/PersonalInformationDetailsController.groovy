package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.person.PersonAddressService
import net.hedtech.banner.general.person.PersonAddressCompositeService
import net.hedtech.banner.general.person.PersonAddressUtility
import net.hedtech.banner.general.person.PersonTelephoneService
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.overall.AddressRolePrivilegesCompositeService
import net.hedtech.banner.general.system.CountyService
import net.hedtech.banner.general.system.StateService
import net.hedtech.banner.general.system.NationService
import net.hedtech.banner.general.system.TelephoneTypeService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.core.context.SecurityContextHolder

class PersonalInformationDetailsController {
    def addressRolePrivilegesCompositeService
    def countyService
    def stateService
    def nationService
    def personAddressService
    def personAddressCompositeService
    def personAddressByRoleViewService
    def emailTypeService
    def telephoneTypeService
    def personEmailService
    def personEmailCompositeService
    def personTelephoneService
    def personalInformationCompositeService

    private def findPerson() {
        return PersonUtility.getPerson(PersonalInformationControllerUtility.getPrincipalPidm())
    }

    def getAddresses() {
        def model = [:]
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        if (pidm) {
            def addresses

            try {
                addresses = personAddressByRoleViewService.getActiveAddressesByRoles(getRoles(), pidm)
            } catch (ApplicationException e) {
                render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            }

            // TODO: if masking turns out to be needed here, search for "maskingRule" in EmployeeProfileController.groovy
            // TODO: in Employee Profile app.
            model.addresses = []

            def personAddress

            addresses.each { it ->
                personAddress = [:]
                personAddress.id = it.id
                personAddress.version = it.version
                personAddress.addressType = [code: it.addressType, description: it.addressTypeDescription]
                personAddress.fromDate = it.fromDate
                personAddress.toDate = it.toDate
                personAddress.isFuture = isDateInFuture(it.fromDate)
                personAddress.houseNumber = it.houseNumber
                personAddress.streetLine1 = it.streetLine1
                personAddress.streetLine2 = it.streetLine2
                personAddress.streetLine3 = it.streetLine3
                personAddress.streetLine4 = it.streetLine4
                personAddress.city = it.city
                personAddress.county = [code: it.countyCode, description: it.county]
                personAddress.state = [code: it.stateCode, description: it.state]
                personAddress.zip = it.zip
                personAddress.nation = [code: it.nationCode, nation: it.nation]
                personAddress.displayAddress = PersonAddressUtility.formatDefaultAddress(
                        [houseNumber:it.houseNumber,
                         streetLine1:it.streetLine1,
                         streetLine2:it.streetLine2,
                         streetLine3:it.streetLine3,
                         streetLine4:it.streetLine4,
                         city:it.city,
                         state:it.state,
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
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render countyService.fetchCountyList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getStateList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render stateService.fetchStateList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getNationList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render nationService.fetchNationList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getAddressTypeList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render addressRolePrivilegesCompositeService.fetchUpdateableAddressTypeList(getRoles(), map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def addAddress() {
        def newAddress = request?.JSON ?: params
        newAddress.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(newAddress)

        try {
            personAddressService.checkAddressFieldsValid(newAddress)

            convertAddressDates(newAddress)

            newAddress = personalInformationCompositeService.getAddressValidationObjects(getRoles(), newAddress)

            def addresses = []
            addresses[0] = [:]
            addresses[0].personAddress = newAddress
            personAddressCompositeService.createOrUpdate([createPersonAddressTelephones: addresses])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateAddress() {
        def updatedAddress = request?.JSON ?: params
        updatedAddress.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(updatedAddress)

        // Address type is a primary key field and cannot be modified
        updatedAddress.remove("addressType")

        try {
            personAddressService.checkAddressFieldsValid(updatedAddress)

            convertAddressDates(updatedAddress)

            personAddressService.update(updatedAddress)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteAddress() {
        def map = request?.JSON ?: params

        try {
            def result = personAddressService.delete(map)

            render([failure: false] as JSON)

        } catch (ApplicationException e) {
            def result = PersonProfileControllerUtility.returnFailureMessage(e)

            render result as JSON
        }
    }

    def getEmails() {
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model = [:]

        if (pidm) {
            model.emails = personEmailService.fetchByPidmAndActiveAndWebDisplayable(pidm)
            JSON.use("deep") {
                render model as JSON
            }
        }
    }

    def getEmailTypeList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render emailTypeService.fetchEmailTypeList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def addEmail() {
        def newEmail = request?.JSON ?: params
        newEmail.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(newEmail)

        try {
            newEmail.emailType = emailTypeService.fetchByCodeAndWebDisplayable(newEmail.emailType.code)

            def emails = []
            emails[0] = newEmail
            personEmailCompositeService.createOrUpdate([personEmails: emails])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getTelephoneNumbers() {
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model = [:]

        if (pidm) {
            try {
                model.telephones = personTelephoneService.fetchActiveTelephonesByPidm(pidm, true)
            } catch (ApplicationException e) {
                render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            }

            JSON.use("deep") {
                render model as JSON
            }
        }
    }

    def getTelephoneTypeList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render telephoneTypeService.fetchUpdateableTelephoneTypeList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
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

    private def convertAddressDates(addressMap){
        // Convert date Strings to Date objects
        addressMap.fromDate = DateUtility.parseDateString(addressMap.fromDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
        if(addressMap.toDate)
            addressMap.toDate = DateUtility.parseDateString(addressMap.toDate, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
    }

    private def isDateInFuture(date) {
        Date today = new Date()
        return date.after(today)
    }
}