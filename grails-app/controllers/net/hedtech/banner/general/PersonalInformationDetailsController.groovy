package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.person.PersonAddressUtility
import net.hedtech.banner.general.person.PersonUtility
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
    def relationshipService
    def personEmergencyContactService

    private def findPerson() {
        return PersonUtility.getPerson(PersonalInformationControllerUtility.getPrincipalPidm())
    }

    def getMaskingRules() {
        def maskingRules = [:]

        try {
            maskingRules = PersonalInformationControllerUtility.getMaskingRule('PERSONALINFORMATION')
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }

        render maskingRules as JSON
    }

    def getAddresses() {
        def model = [:]
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        if (pidm) {
            def addresses
            def maskingRule

            try {
                maskingRule = PersonalInformationControllerUtility.getMaskingRule('PERSONALINFORMATION')

                addresses = personAddressByRoleViewService.getActiveAddressesByRoles(getRoles(), pidm)
            } catch (ApplicationException e) {
                render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            }

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
                personAddress.isUpdateable = (it.priviledgeIndicator == 'U')
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
                         country:it.nation,
                         displayHouseNumber:maskingRule.displayHouseNumber,
                         displayStreetLine4:maskingRule.displayStreetLine4])

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

            newAddress = personalInformationCompositeService.getPersonValidationObjects(newAddress, getRoles())

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

        try {
            personAddressService.checkAddressFieldsValid(updatedAddress)

            updatedAddress = personalInformationCompositeService.getPersonValidationObjects(updatedAddress, getRoles())

            convertAddressDates(updatedAddress)
            personAddressCompositeService.checkDatesForUpdate(updatedAddress)

            personAddressService.update(updatedAddress)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteAddress() {
        def map = request?.JSON ?: params

        // We don't actually delete; we inactivate
        def addressToDelete = [
                id: map.id,
                version: map.version,
                statusIndicator: 'I'
        ]

        try {
            personAddressService.update(addressToDelete)

            render([failure: false] as JSON)

        } catch (ApplicationException e) {
            def result = PersonalInformationControllerUtility.returnFailureMessage(e)

            render result as JSON
        }
    }

    def getEmails() {
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model = [:]

        if (pidm) {
            model.emails = personEmailService.getDisplayableEmails(pidm)
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
            personEmailService.updatePreferredEmail(newEmail)
            personEmailCompositeService.createOrUpdate([personEmails: emails])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateEmail() {
        def updatedEmail = request?.JSON ?: params
        updatedEmail.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(updatedEmail)

        try {
            updatedEmail.emailType = emailTypeService.fetchByCodeAndWebDisplayable(updatedEmail.emailType.code)

            def emails = []
            emails[0] = personEmailService.castEmailForUpdate(updatedEmail)
            personEmailService.updatePreferredEmail(updatedEmail)
            personEmailCompositeService.createOrUpdate([personEmails: emails])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteEmail() {
        def deletedEmail = request?.JSON ?: params
        deletedEmail.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            deletedEmail.emailType = emailTypeService.fetchByCodeAndWebDisplayable(deletedEmail.emailType.code)

            personEmailService.inactivateEmail(deletedEmail)

            render([failure: false] as JSON)

        } catch (ApplicationException e) {
            def result = PersonalInformationControllerUtility.returnFailureMessage(e)

            render result as JSON
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

    def getRelationshipList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render relationshipService.fetchRelationshipList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getEmergencyContacts() {
        def model = [:]
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        if (pidm) {
            def contacts
            def maskingRule

            try {
                maskingRule = PersonalInformationControllerUtility.getMaskingRule('PERSONALINFORMATION')

                contacts = personEmergencyContactService.getEmergencyContactsByPidm(pidm)
            } catch (ApplicationException e) {
                render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            }

            model.emergencyContacts = []

            def emerContact

            contacts.each { it ->
                emerContact = [:]
                emerContact.id = it.id
                emerContact.version = it.version
                emerContact.relationship = it.relationship ? [code: it.relationship.code, description: it.relationship.description] : [:]
                emerContact.phoneArea = it.phoneArea
                emerContact.phoneNumber = it.phoneNumber
                emerContact.phoneExtension = it.phoneExtension
                emerContact.countryPhone = it.countryPhone
                emerContact.priority = it.priority
                emerContact.firstName = it.firstName
                emerContact.middleInitial = it.middleInitial
                emerContact.lastName = it.lastName
                emerContact.houseNumber = it.houseNumber
                emerContact.streetLine1 = it.streetLine1
                emerContact.streetLine2 = it.streetLine2
                emerContact.streetLine3 = it.streetLine3
                emerContact.streetLine4 = it.streetLine4
                emerContact.city = it.city
                emerContact.state = it.state ? [code: it.state.code, description: it.state.description] : [:]
                emerContact.zip = it.zip
                emerContact.nation = it.nation ? [code: it.nation.code, nation: it.nation.nation] : [:]
                emerContact.displayAddress = PersonAddressUtility.formatDefaultAddress(
                        [houseNumber: it.houseNumber,
                         streetLine1: it.streetLine1,
                         streetLine2: it.streetLine2,
                         streetLine3: it.streetLine3,
                         streetLine4: it.streetLine4,
                         city: it.city,
                         state: it.state ? it.state.description : null,
                         zip: it.zip,
                         country: it.nation ? it.nation.nation : null,
                         displayHouseNumber: maskingRule.displayHouseNumber,
                         displayStreetLine4: maskingRule.displayStreetLine4])

                model.emergencyContacts << emerContact
            }

        }

        JSON.use("deep") {
            render model as JSON
        }
    }

    def addEmergencyContact() {
        def newContact = request?.JSON ?: params
        newContact.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(newContact)

        try {
            personEmergencyContactService.checkEmergencyContactFieldsValid(newContact)
            newContact = personalInformationCompositeService.getPersonValidationObjects(newContact)

            personEmergencyContactService.createOrUpdate(newContact)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateEmergencyContact() {
        def updatedContact = request?.JSON ?: params
        updatedContact.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        fixJSONObjectForCast(updatedContact)

        try {
            personEmergencyContactService.checkEmergencyContactFieldsValid(updatedContact)

            personEmergencyContactService.update(updatedContact)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteEmergencyContact() {
        def deletedContact = request?.JSON ?: params
        deletedContact.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            personEmergencyContactService.delete(deletedContact)
            //TODO compress remaining priorities after delete
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
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
        if (!date) {return false}

        Date today = new Date()
        return date.after(today)
    }
}
