/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general

import net.hedtech.banner.general.person.PersonPictureBaseController
import net.hedtech.banner.general.person.PersonUtility

class PersonalInformationPictureController extends PersonPictureBaseController {

    /**
     * Ensure that the caller has access.
     * @param params the controller params set by the request, i.e. the bannerId
     * @return True if the logged in user is the user with that bannerId
     */
    boolean hasAccess() {
        if (!params.bannerId) {
            return false
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def person = PersonUtility.getPerson(params.bannerId)

        return (pidm == person?.pidm)
    }

}
