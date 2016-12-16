/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.person.PersonIdentificationNameCurrent

class PersonalInformationService {
    def getCurrentName(pidm) {
        def currentName = PersonIdentificationNameCurrent.fetchByPidm(pidm)

        return currentName
    }
}
