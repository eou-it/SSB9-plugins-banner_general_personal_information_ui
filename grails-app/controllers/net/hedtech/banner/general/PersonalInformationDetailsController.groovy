/******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 ******************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.person.MedicalInformation
import net.hedtech.banner.general.person.PersonAddressUtility
import net.hedtech.banner.general.person.PersonUtility
import org.apache.commons.lang3.StringEscapeUtils
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
    def preferredNameService
    def personalInformationConfigService
    def personIdentificationNameCurrentService
    def maritalStatusService
    def personBasicPersonBaseService
    def personRaceCompositeService
    def personGenderPronounCompositeService
    def directoryProfileCompositeService
    def medicalInformationCompositeService
    def disabilityService
    def medicalConditionService
    def securityQAService


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
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.ADDR_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_READONLY
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

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
            def addressDisplaySequence = personalInformationConfigService.getSequenceConfiguration(PersonalInformationConfigService.OVERVIEW_ADDR)
            def personAddress

            addresses.each { it ->
                personAddress = [:]
                personAddress.id = it.id
                personAddress.version = it.version
                personAddress.addressType = [code: it.addressType, description: it.addressTypeDescription]
                personAddress.displayPriority = addressDisplaySequence ? addressDisplaySequence[personAddress.addressType.code] : null
                personAddress.fromDate = it.fromDate
                personAddress.toDate = it.toDate
                personAddress.isFuture = isDateInFuture(it.fromDate)
                personAddress.houseNumber = it.houseNumber
                personAddress.streetLine1 = it.streetLine1
                personAddress.streetLine2 = it.streetLine2
                personAddress.streetLine3 = it.streetLine3
                personAddress.streetLine4 = it.streetLine4
                personAddress.city = it.city
                personAddress.county = it.countyCode ? [code: it.countyCode, description: it.county] : null
                personAddress.state = it.stateCode ? [code: it.stateCode, description: it.state] : null
                personAddress.zip = it.zip
                personAddress.nation = it.nationCode ? [code: it.nationCode, nation: it.nation] : null
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
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.ADDR_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def newAddress = request?.JSON ?: params
        newAddress.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        newAddress = unescapeHtml(newAddress, ["state", "nation", "county", "addressType"])

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
        try {
            checkActionPermittedPerConfiguration([
                    name: PersonalInformationConfigService.ADDR_MODE,
                    minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedAddress = request?.JSON ?: params
        updatedAddress.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        updatedAddress = unescapeHtml(updatedAddress, ["state", "nation", "county", "addressType"])
        try {
            personAddressService.checkAddressFieldsValid(updatedAddress)

            updatedAddress = personalInformationCompositeService.getPersonValidationObjects(updatedAddress, getRoles())

            convertAddressDates(updatedAddress)
            personAddressCompositeService.checkDatesForUpdate(updatedAddress)

            def addresses = []
            addresses[0] = [:]
            addresses[0].personAddress = updatedAddress
            personAddressCompositeService.createOrUpdate([createPersonAddressTelephones: addresses], false)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteAddress() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.ADDR_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def map = request?.JSON ?: params

        // We don't actually delete; we inactivate
        def addressToDelete = [
                id: map.id,
                version: map.version,
                statusIndicator: 'I'
        ]

        try {
            addressRolePrivilegesCompositeService.fetchAddressType(getRoles(), map.addressType.code)

            personAddressService.update(addressToDelete)

            render([failure: false] as JSON)

        } catch (ApplicationException e) {
            def result = PersonalInformationControllerUtility.returnFailureMessage(e)

            render result as JSON
        }
    }

    def getEmails() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMAIL_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_READONLY
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model = [:]

        if (pidm) {
            model.emails = personEmailService.getDisplayableEmails(pidm)
            personalInformationCompositeService.populateEmailUpdateableStatus(model.emails, getRoles())
            def prefEmail = personEmailService.findPreferredEmailAddress(pidm)
            model.isPreferredEmailVisible = prefEmail ? prefEmail.displayWebIndicator : true

            JSON.use("deep") {
                render model as JSON
            }
        }
    }

    def getEmailTypeList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            render personalInformationCompositeService.fetchUpdateableEmailTypeList(pidm, getRoles(), map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def addEmail() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMAIL_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def newEmail = request?.JSON ?: params
        newEmail.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        newEmail = unescapeHtml(newEmail, ["emailType"])

        try {
            newEmail.emailType = emailTypeService.fetchByCodeAndWebDisplayable(newEmail.emailType.code)
            personalInformationCompositeService.validateEmailTypeRule(newEmail.emailType, newEmail.pidm, getRoles())

            def emails = []
            personEmailService.updatePreferredEmail(newEmail)
            newEmail = personEmailService.updateIfExistingEmail(newEmail)
            emails[0] = newEmail
            personEmailCompositeService.createOrUpdate([personEmails: emails])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateEmail() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMAIL_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedEmail = request?.JSON ?: params
        updatedEmail.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        updatedEmail = unescapeHtml(updatedEmail, ["emailType"])
        try {
            updatedEmail.emailType = emailTypeService.fetchByCodeAndWebDisplayable(updatedEmail.emailType.code)
            personalInformationCompositeService.validateEmailTypeRule(updatedEmail.emailType, updatedEmail.pidm, getRoles())

            def emails = []
            updatedEmail.remove('isUpdateable')  //Remove field which is not actually part of domain class
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
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMAIL_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def deletedEmail = request?.JSON ?: params
        deletedEmail.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            deletedEmail.emailType = emailTypeService.fetchByCodeAndWebDisplayable(deletedEmail.emailType.code)
            personalInformationCompositeService.validateEmailTypeRule(deletedEmail.emailType, deletedEmail.pidm, getRoles())

            deletedEmail.remove('isUpdateable')  //Remove field which is not actually part of domain class
            personEmailService.inactivateEmail(deletedEmail)

            render([failure: false] as JSON)

        } catch (ApplicationException e) {
            def result = PersonalInformationControllerUtility.returnFailureMessage(e)

            render result as JSON
        }
    }

    def getTelephoneNumbers() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.PHONE_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_READONLY
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model = [:]

        if (pidm) {
            try {
                model.telephones = personTelephoneService.fetchActiveTelephonesByPidm(pidm, null, true,
                        personalInformationConfigService.getSequenceConfiguration(PersonalInformationConfigService.OVERVIEW_PHONE))
                personalInformationCompositeService.populateTelephoneUpdateableStatus(model.telephones, getRoles())
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
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            render personalInformationCompositeService.fetchUpdateableTelephoneTypeList(pidm, getRoles(), map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def addTelephoneNumber() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.PHONE_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def newPhoneNumber = request?.JSON ?: params
        newPhoneNumber.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        newPhoneNumber = unescapeHtml(newPhoneNumber, ["telephoneType"])

        try {
            newPhoneNumber.telephoneType = telephoneTypeService.fetchValidByCode(newPhoneNumber.telephoneType.code)
            personalInformationCompositeService.validateTelephoneTypeRule(newPhoneNumber.telephoneType, newPhoneNumber.pidm, getRoles())

            personTelephoneService.create(newPhoneNumber)
            render([failure: false] as JSON)
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateTelephoneNumber() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.PHONE_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedPhoneNumber = request?.JSON ?: params
        updatedPhoneNumber.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        updatedPhoneNumber = unescapeHtml(updatedPhoneNumber, ["telephoneType"])

        try {
            updatedPhoneNumber.telephoneType = telephoneTypeService.fetchValidByCode(updatedPhoneNumber.telephoneType.code)
            personalInformationCompositeService.validateTelephoneTypeRule(updatedPhoneNumber.telephoneType, updatedPhoneNumber.pidm, getRoles())

            personTelephoneService.inactivateAndCreate(updatedPhoneNumber)
            render([failure: false] as JSON)
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteTelephoneNumber() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.PHONE_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def deletedPhoneNumber = request?.JSON ?: params
        deletedPhoneNumber.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            deletedPhoneNumber.telephoneType = telephoneTypeService.fetchValidByCode(deletedPhoneNumber.telephoneType.code)
            personalInformationCompositeService.validateTelephoneTypeRule(deletedPhoneNumber.telephoneType, deletedPhoneNumber.pidm, getRoles())

            personTelephoneService.inactivatePhone(deletedPhoneNumber)
            render([failure: false] as JSON)
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
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMER_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_READONLY
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def model = [:]
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        if (pidm) {
            def contacts

            try {
                contacts = personEmergencyContactService.getEmergencyContactsByPidm(pidm)
            } catch (ApplicationException e) {
                render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            }

            model = populateEmergencyContactsModel(contacts)
        }

        JSON.use("deep") {
            render model as JSON
        }
    }

    def addEmergencyContact() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMER_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def newContact = request?.JSON ?: params
        newContact.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        newContact = unescapeHtml(newContact, ["relationship", "state", "nation"])
        try {
            personEmergencyContactService.checkEmergencyContactFieldsValid(newContact)
            newContact = personalInformationCompositeService.getPersonValidationObjects(newContact)

            personEmergencyContactService.createEmergencyContactWithPriorityShuffle(newContact)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateEmergencyContact() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMER_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedContact = request?.JSON ?: params
        updatedContact.pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        updatedContact = unescapeHtml(updatedContact, ["relationship", "state", "nation"])
        try {
            personEmergencyContactService.checkEmergencyContactFieldsValid(updatedContact)
            updatedContact = personalInformationCompositeService.getPersonValidationObjects(updatedContact)

            personEmergencyContactService.updateEmergencyContactWithPriorityShuffle(updatedContact)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def deleteEmergencyContact() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.EMER_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def deletedContact = request?.JSON ?: params
        deletedContact.pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            personEmergencyContactService.deleteEmergencyContactWithPriorityShuffle(deletedContact)
            def contacts = personEmergencyContactService.getEmergencyContactsByPidm(deletedContact.pidm)
            def model = populateEmergencyContactsModel(contacts)

            JSON.use("deep") {
                render model as JSON
            }
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getPreferredName() {

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def usage = preferredNameService.getUsage(params.pageName, params.sectionName)
        def model = [:]

        model.preferredName = preferredNameService.getPreferredName([pidm: pidm, usage: usage])

        try {
            render model as JSON
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    //get the disability classification status of the person. Return the internal sequence of the
    //equivalent sdax record for the DISA value.
    def getDisabilityStatus() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.DISABILITY_STATUS,
                  minRequiredMode: PersonalInformationConfigService.YES
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model =[:]
        try {
            MedicalInformation mi = MedicalInformation.fetchByPidmForDisabSurvey(pidm)
            model.disabilityStatusCode = mi?.id ? mi.disability.code : 0
            render model as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateDisabilityStatus() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.DISABILITY_STATUS,
                  minRequiredMode: PersonalInformationConfigService.YES
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedDisability = request?.JSON ?: params
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        try {
            def medicalInfo = MedicalInformation.fetchByPidmForDisabSurvey(pidm)
            if(!medicalInfo) {
                medicalInfo = new MedicalInformation([
                        pidm: pidm,
                        disabilityIndicator: false,
                        medicalCondition: medicalConditionService.fetchMedicalCondition('DISABSURV')
                ])
            }
            medicalInfo.disability = disabilityService.fetchDisability(updatedDisability.code)

            medicalInformationCompositeService.createOrUpdate([medicalInformations: [medicalInfo]])
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateVeteranClassification() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.VETERANS_CLASSIFICATION,
                  minRequiredMode: PersonalInformationConfigService.YES
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedValues = request?.JSON ?: params

        def person = [
                pidm: PersonalInformationControllerUtility.getPrincipalPidm(),
                id: updatedValues.id,
                version: updatedValues.version,
                veraIndicator: updatedValues.veraIndicator,
                activeDutySeprDate: parseJavaScriptDate(updatedValues.activeDutySeprDate),
                sdvetIndicator: updatedValues.sdvetIndicator,
                armedServiceMedalVetIndicator: updatedValues.armedServiceMedalVetIndicator
        ]

        try {
            personGenderPronounCompositeService.checkPersonBaseExists(person)
            personBasicPersonBaseService.update(person)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }

    }


    def getUserName() {
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def model = [:]

        try {
            def userName = personIdentificationNameCurrentService.getCurrentNameByPidm(pidm)

            model.firstName = userName.firstName
            model.middleName = userName.middleName
            model.lastName = userName.lastName

            render model as JSON
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getBannerId() {
        def person = findPerson()
        def model = [:]

        model.bannerId = person?.bannerId

        try {
            render model as JSON
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getMaritalStatusList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render maritalStatusService.fetchMaritalStatusList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getGenderList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render personGenderPronounCompositeService.fetchGenderList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getPronounList() {
        def map = PersonalInformationControllerUtility.getFetchListParams(params)

        try {
            render personGenderPronounCompositeService.fetchPronounList(map.max, map.offset, map.searchString) as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getPersonalDetails() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.PERS_DETAILS_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_READONLY
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            def model = personGenderPronounCompositeService.fetchPersonalDetails(pidm, personalInformationConfigService.getFieldDisplayConfigurationsHashMap())
            model = removeUnauthorizedFieldsFromPersonalDetails(model)
            if (!model) {
                model = [:] // Force it to be a map, which is what is expected to be rendered
            }

            render model as JSON

        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    /**
     *Get personal details returns a combination of personal details and veteran classification.
     *When either of these are disabled in GUROCFG, they should be removed from the model
     *so they are not revealed to the front-end or though http requests.
     */
    private static removeUnauthorizedFieldsFromPersonalDetails(model){
        def personalDetailsSectionEnabled = Holders?.config?.'personalInfo.personalDetailSectionMode' != 0
        def veteranClassificationFieldEnabled = Holders?.config?.'personalInfo.additionalDetails.veteranClassificationMode' != 0
        if (!personalDetailsSectionEnabled){
            model = removePersonalDetailsSectionFieldsFromModel(model)
        }
        if(!veteranClassificationFieldEnabled){
            model = removeVeteranClassificationFromModel(model)
        }
        model
    }

    private static removePersonalDetailsSectionFieldsFromModel(model){
        def personalDetailsSectionFields = ['preferenceFirstName', 'sex', 'birthDate', 'maritalStatus',
        'gender', 'pronoun']
        personalDetailsSectionFields.each {field ->
            model?.remove(field)
        }
        model
    }

    private static removeVeteranClassificationFromModel(model){
        def veteranClassificationFields = ['activeDutySeprDate', 'armedServiceMedalVetIndicator', 'sdvetIndicator',
        'vetcFileNumber', 'veraIndicator']
        veteranClassificationFields.each {field ->
            model?.remove(field)
        }
        model
    }

    def updatePersonalDetails() {
        try {
            checkActionPermittedPerConfiguration([
                    name           : PersonalInformationConfigService.PERS_DETAILS_MODE,
                    minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }
        def updatedPerson = request?.JSON ?: params
        updatedPerson = unescapeHtml(updatedPerson, ["maritalStatus", "pronoun", "gender"])
        def person = [
                pidm               : PersonalInformationControllerUtility.getPrincipalPidm(),
                id                 : updatedPerson.id,
                version            : updatedPerson.version,
                preferenceFirstName: updatedPerson.preferenceFirstName
        ]

        //Only include fields which are authorized to be updated
        personalInformationConfigService.getFieldDisplayConfigurationsHashMap().forEach({ key, value ->
            if (personalInformationConfigService.isFieldUpdateable(value)){
                if (updatedPerson.containsKey(key)){
                    person.put(key, updatedPerson.get(key))
                }
            }
        })

        try {
            personGenderPronounCompositeService.updatePerson(person)
            render([failure: false] as JSON)
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getRaces() {
        def model = [:]
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            model.races = personRaceCompositeService.getRacesByPidm(pidm).collect {
                it = [description: it.description, race: it.race]
            }
            render model as JSON
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getDirectoryProfile() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.DIRECTORY_PROFILE,
                  minRequiredMode: PersonalInformationConfigService.YES
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def model = [:]
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        try {
            def addrMaskingRule = PersonalInformationControllerUtility.getMaskingRule('PERSONALINFORMATION')
            model.directoryProfile = directoryProfileCompositeService.fetchDirectoryProfileItemsForUser(pidm, addrMaskingRule)
            render model as JSON
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updateDirectoryProfilePreferences() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.DIRECTORY_PROFILE,
                  minRequiredMode: PersonalInformationConfigService.YES
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def preferences = request?.JSON ?: params

        try {
            directoryProfileCompositeService.createOrUpdate(pidm, preferences)
            render([failure: false] as JSON)
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def getPiConfig() {
        def model = [:]
        personalInformationConfigService.updateFieldDisplayConfigurations()
        try {
            model = personalInformationConfigService.getUpdatedPersonalInformationConfigurations(model)
            def personConfig = PersonUtility.getPersonConfigFromSession()
            def noOfQuestions = personConfig[PersonalInformationConfigService.NO_OF_QSTNS]
            if(noOfQuestions == null) {
                Map result = securityQAService.getUserDefinedPreference()
                if (result != null) {
                    noOfQuestions = result.GUBPPRF_NO_OF_QSTNS?.intValue()
                }
                personConfig[PersonalInformationConfigService.NO_OF_QSTNS] = noOfQuestions
                PersonUtility.setPersonConfigInSession(personConfig)
            }
            model = personalInformationConfigService.getOtherSectionConfigurations(model, noOfQuestions)
            render model as JSON
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    private def getRoles() {
        def roles = SecurityContextHolder?.context?.authentication?.principal?.authorities.collect {
            if (it.authority.contains('ROLE_SELFSERVICE')) {
                it.getAssignedSelfServiceRole()
            }
        }

        return roles.unique()
    }

    private def convertAddressDates(addressMap) {
        addressMap.fromDate = parseJavaScriptDate(addressMap.fromDate)
        addressMap.toDate = parseJavaScriptDate(addressMap.toDate)
    }

    private def parseJavaScriptDate(date) {
        // Convert date Strings to Date objects
        if(date) {
            return DateUtility.parseDateString(date, "yyyy-MM-dd'T'HH:mm:ss.sss'Z'")
        }
        else {
            return null
        }
    }

    private def isDateInFuture(date) {
        if (!date) {return false}

        Date today = new Date()
        return date.after(today)
    }

    private def checkActionPermittedPerConfiguration(param) {
        // Make sure this operation is permitted based on configuration.
        // (If the configuration is not set to allow updates, that functionality should not be available
        // in the UI in the first place, however, to prevent spoofing, etc. we make a check here as well.)
        def SECTION_UPDATEABLE = PersonalInformationConfigService.SECTION_UPDATEABLE
        def mode = personalInformationConfigService.getParamFromSession(param.name, SECTION_UPDATEABLE)
        if (!mode){
            mode = Holders?.config?.get(param.name)
        }
        def associatedMode   //query even if hidden if associated entity is displayable
        if (param.name == PersonalInformationConfigService.EMAIL_MODE) {
            associatedMode = Holders?.config?.get(PersonalInformationConfigService.DISPLAY_OVERVIEW_EMAIL)
        } else if (param.name == PersonalInformationConfigService.ADDR_MODE) {
            associatedMode = Holders?.config?.get(PersonalInformationConfigService.DISPLAY_OVERVIEW_ADDR)
        } else if (param.name == PersonalInformationConfigService.PHONE_MODE) {
            associatedMode = Holders?.config?.get(PersonalInformationConfigService.DISPLAY_OVERVIEW_PHONE)
        } else if (param.name == PersonalInformationConfigService.PERS_DETAILS_MODE) {
            //Get the more restrictive setting out of the additional details section or the veterans classification field.
            associatedMode = PersonalInformationConfigService.getMostRestrictiveAdditionalDetailsSetting(Holders?.config?.get(PersonalInformationConfigService.VETERANS_CLASSIFICATION))
        } else
            associatedMode = 'N'

        if (modeIsNotSetToEnabled(mode)) {
            if (mode == 'N' || (mode == PersonalInformationConfigService.SECTION_HIDDEN && (associatedMode == 'N' || associatedMode == 0)) ||
                (param.minRequiredMode == SECTION_UPDATEABLE && mode != SECTION_UPDATEABLE)) {

                log.error("Unauthorized attempt to access Personal Information data was prevented. Configured value for parameter ${param.name}: ${mode}")
                throw new ApplicationException(PersonalInformationDetailsController, "@@r1:operation.not.authorized@@")
            }
        }
    }

    private static modeIsNotSetToEnabled(mode){
        return mode != 'Y' && mode != 1 && mode != 2
    }

    /**
     *Unescapes any HTML that may appear as a result of special character codes being entered in the database by users
     * and being interpreted as escaped html entities.
     */
    private unescapeHtml(map, propertiesThatCouldHaveEscapedCodes){
        propertiesThatCouldHaveEscapedCodes?.each{ property ->
            map?.getAt(property)?.code = StringEscapeUtils.unescapeHtml4(map?.getAt(property)?.code)
        }
        map
    }

    /**
     * Create UI-friendly model from emergency contact domain objects
     * @param contacts Emergency contact domain objects
     * @return UI-friendly model
     */
    private def populateEmergencyContactsModel(contacts) {
        def model = [
                emergencyContacts: []
        ]

        def maskingRule = PersonalInformationControllerUtility.getMaskingRule('PERSONALINFORMATION')
        def emerContact

        contacts.each { it ->
            emerContact = [:]
            emerContact.id = it.id
            emerContact.version = it.version
            emerContact.relationship = it.relationship ? [code: it.relationship.code, description: it.relationship.description] : null
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
            emerContact.state = it.state ? [code: it.state.code, description: it.state.description] : null
            emerContact.zip = it.zip
            emerContact.nation = it.nation ? [code: it.nation.code, nation: it.nation.nation] : null
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

        model
    }
}
