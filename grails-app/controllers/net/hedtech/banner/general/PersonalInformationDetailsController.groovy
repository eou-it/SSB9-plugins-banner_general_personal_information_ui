/******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 ******************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
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

            // Define configuration to fetch phone sequence from GORICCR
            def sequenceConfig = [processCode: 'PERSONAL_INFORMATION_SSB', settingName: PersonalInformationConfigService.OVERVIEW_ADDR]
            def addressDisplaySequence = PersonUtility.getDisplaySequence('addressDisplaySequence', sequenceConfig)
            def personAddress

            addresses.each { it ->
                personAddress = [:]
                personAddress.id = it.id
                personAddress.version = it.version
                personAddress.addressType = [code: it.addressType, description: it.addressTypeDescription]
                personAddress.displayPriority = addressDisplaySequence[personAddress.addressType.code]
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
        newAddress.state?.code = StringEscapeUtils.unescapeHtml4(newAddress.state?.code)
        newAddress.nation?.code = StringEscapeUtils.unescapeHtml4(newAddress.nation?.code)
        newAddress.county?.code = StringEscapeUtils.unescapeHtml4(newAddress.county?.code)
        newAddress.addressType?.code = StringEscapeUtils.unescapeHtml4(newAddress.addressType?.code)

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
        updatedAddress.state?.code = StringEscapeUtils.unescapeHtml4(updatedAddress.state?.code)
        updatedAddress.nation?.code = StringEscapeUtils.unescapeHtml4(updatedAddress.nation?.code)
        updatedAddress.county?.code = StringEscapeUtils.unescapeHtml4(updatedAddress.county?.code)
        updatedAddress.addressType?.code = StringEscapeUtils.unescapeHtml4(updatedAddress.addressType?.code)

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
        newEmail.emailType?.code = StringEscapeUtils.unescapeHtml4(newEmail.emailType?.code)

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
        updatedEmail.emailType?.code = StringEscapeUtils.unescapeHtml4(updatedEmail.emailType?.code)

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
                // Define configuration to fetch phone sequence from GORICCR
                def sequenceConfig = [processCode: 'PERSONAL_INFORMATION_SSB', settingName: PersonalInformationConfigService.OVERVIEW_PHONE]

                model.telephones = personTelephoneService.fetchActiveTelephonesByPidm(pidm, sequenceConfig, true)
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
        newPhoneNumber.telephoneType?.code = StringEscapeUtils.unescapeHtml4(newPhoneNumber.telephoneType?.code)

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
        updatedPhoneNumber.telephoneType?.code = StringEscapeUtils.unescapeHtml4(updatedPhoneNumber.telephoneType?.code)

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
        newContact.relationship?.code = StringEscapeUtils.unescapeHtml4(newContact.relationship?.code)
        newContact.state?.code = StringEscapeUtils.unescapeHtml4(newContact.state?.code)
        newContact.nation?.code = StringEscapeUtils.unescapeHtml4(newContact.nation?.code)

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
        updatedContact.relationship?.code = StringEscapeUtils.unescapeHtml4(updatedContact.relationship?.code)
        updatedContact.state?.code = StringEscapeUtils.unescapeHtml4(updatedContact.state?.code)
        updatedContact.nation?.code = StringEscapeUtils.unescapeHtml4(updatedContact.nation?.code)


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
            def model = personGenderPronounCompositeService.fetchPersonalDetails(pidm)

            if (!model) {
                model = [:] // Force it to be a map, which is what is expected to be rendered
            }

            render model as JSON
        }
        catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }
    }

    def updatePersonalDetails() {
        try {
            checkActionPermittedPerConfiguration([
                  name: PersonalInformationConfigService.PERS_DETAILS_MODE,
                  minRequiredMode: PersonalInformationConfigService.SECTION_UPDATEABLE
            ])
        } catch (ApplicationException e) {
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
            return
        }

        def updatedPerson = request?.JSON ?: params
        updatedPerson.maritalStatus?.code = StringEscapeUtils.unescapeHtml4(updatedPerson.maritalStatus?.code)
        def person = [
                pidm: PersonalInformationControllerUtility.getPrincipalPidm(),
                id: updatedPerson.id,
                version: updatedPerson.version,
                preferenceFirstName: updatedPerson.preferenceFirstName,
                maritalStatus: updatedPerson.maritalStatus
        ]

        if(personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.GENDER_PRONOUN, 'Y') == 'Y') {
            updatedPerson.gender?.code = StringEscapeUtils.unescapeHtml4(updatedPerson.gender?.code)
            updatedPerson.pronoun?.code = StringEscapeUtils.unescapeHtml4(updatedPerson.pronoun?.code)
            person.gender = updatedPerson.gender
            person.pronoun = updatedPerson.pronoun
        }


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

        try {
            model.isPreferredEmailUpdateable =     personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.PREF_EMAIL, 'Y') == 'Y'
            model.isProfilePicDisplayable =        personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.PROFILE_PICTURE, 'Y') == 'Y'
            model.isOverviewAddressDisplayable =   personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISPLAY_OVERVIEW_ADDR, 'Y') == 'Y'
            model.isOverviewPhoneDisplayable =     personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISPLAY_OVERVIEW_PHONE, 'Y') == 'Y'
            model.isOverviewEmailDisplayable =     personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISPLAY_OVERVIEW_EMAIL, 'Y') == 'Y'
            model.isDirectoryProfileDisplayable =  personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DIRECTORY_PROFILE, 'Y') == 'Y'
            model.isVetClassificationDisplayable = personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.VETERANS_CLASSIFICATION, 'Y') == 'Y'
            model.isSecurityQandADisplayable =     personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.SECURITY_QA_CHANGE, 'Y') == 'Y'
            model.isPasswordChangeDisplayable =    personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.PASSWORD_CHANGE, 'Y') == 'Y'
            model.isDisabilityStatusDisplayable =  personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISABILITY_STATUS, 'Y') == 'Y'
            model.isMaritalStatusUpdateable =      personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.MARITAL_STATUS, 'Y') == 'Y'
            model.ethnRaceMode =                   personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.ETHN_RACE_MODE,    PersonalInformationConfigService.SECTION_UPDATEABLE)
            model.emailSectionMode =               personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.EMAIL_MODE,        PersonalInformationConfigService.SECTION_UPDATEABLE)
            model.telephoneSectionMode =           personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.PHONE_MODE,        PersonalInformationConfigService.SECTION_UPDATEABLE)
            model.addressSectionMode =             personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.ADDR_MODE,         PersonalInformationConfigService.SECTION_UPDATEABLE)
            model.emergencyContactSectionMode =    personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.EMER_MODE,         PersonalInformationConfigService.SECTION_UPDATEABLE)
            model.personalDetailsSectionMode =     personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.PERS_DETAILS_MODE, PersonalInformationConfigService.SECTION_UPDATEABLE)
            model.additionalDetailsSectionMode =   (model.ethnRaceMode != PersonalInformationConfigService.SECTION_HIDDEN)||(model.isVetClassificationDisplayable)||(model.isDisabilityStatusDisplayable)
            model.isGenderPronounDisplayable = personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.GENDER_PRONOUN, 'Y') == 'Y'

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

            model.isSecurityQandADisplayable = model.isSecurityQandADisplayable && noOfQuestions > 0
            model.otherSectionMode = (model.isDirectoryProfileDisplayable) || (model.isSecurityQandADisplayable) || (model.isPasswordChangeDisplayable)

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
        def associatedMode   //query even if hidden if associated entity is displayable
        if (param.name == PersonalInformationConfigService.EMAIL_MODE) {
            associatedMode = personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISPLAY_OVERVIEW_EMAIL, 'Y')
        } else if (param.name == PersonalInformationConfigService.ADDR_MODE) {
            associatedMode = personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISPLAY_OVERVIEW_ADDR, 'Y')
        } else if (param.name == PersonalInformationConfigService.PHONE_MODE) {
            associatedMode = personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.DISPLAY_OVERVIEW_PHONE, 'Y')
        } else if (param.name == PersonalInformationConfigService.PERS_DETAILS_MODE) {
            associatedMode = personalInformationConfigService.getParamFromSession(PersonalInformationConfigService.VETERANS_CLASSIFICATION, 'Y')
        } else
            associatedMode = 'N'

        if (mode != 'Y' ) {
            if (mode == 'N' || (mode == PersonalInformationConfigService.SECTION_HIDDEN && associatedMode == 'N') ||
                (param.minRequiredMode == SECTION_UPDATEABLE && mode != SECTION_UPDATEABLE)) {

                log.error("Unauthorized attempt to access Personal Information data was prevented. Configured value for parameter ${param.name}: ${mode}")
                throw new ApplicationException(PersonalInformationDetailsController, "@@r1:operation.not.authorized@@")
            }
        }
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
