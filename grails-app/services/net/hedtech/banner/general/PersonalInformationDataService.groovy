/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.person.PersonTelephoneDecorator

class PersonalInformationDataService {

    def personTelephoneService
    def personalInformationConfigService


    def getTelephones(pidm, session) {
        def telephoneRecords = personTelephoneService.fetchActiveTelephonesByPidm(pidm, true)
        def telephone
        def telephones = []
        def telephoneDisplayPriorities = personalInformationConfigService.getTelephoneDisplayPriorities(session)
        def decorator

        telephoneRecords.each { it ->
            telephone = [:]
            telephone.id = it.id
            telephone.version = it.version
            telephone.telephoneType = it.telephoneType
            telephone.displayPriority = telephoneDisplayPriorities[telephone.telephoneType.code]
            telephone.internationalAccess = it.internationalAccess
            telephone.countryPhone = it.countryPhone
            telephone.phoneArea = it.phoneArea
            telephone.phoneNumber = it.phoneNumber
            telephone.phoneExtension = it.phoneExtension
            telephone.unlistIndicator = it.unlistIndicator

            decorator = new PersonTelephoneDecorator(it)
            telephone.displayPhoneNumber = decorator.displayPhone

            telephones << telephone
        }

        return telephones
    }

}