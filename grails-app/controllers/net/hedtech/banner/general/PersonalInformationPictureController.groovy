/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general

import grails.util.Holders
import net.hedtech.banner.general.person.PersonPictureBaseController
import net.hedtech.banner.general.person.PersonUtility

class PersonalInformationPictureController extends PersonPictureBaseController {

    //TODO: not sure what all will be needed of this
    /**
     * Ensure that the caller has access.
     * @param params The parameters (ignored)
     * @return True if the logged in user is an employee
     */
    boolean hasAccess() {
        log.error "DJD debug: "+getApplicationContext().getResource('images').file.absolutePath
        log.error "DJD debug: "+Holders.config.banner.picturesPath

        if (!params.bannerId) {
            return false
        }

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def person = PersonUtility.getPerson(params.bannerId)

        if (pidm == person?.pidm) {
            return true
        } else {
            return false
        } /*else {
            return EmployeeProfileUtility.hasAccessToEmployee(pidm,person?.pidm)
        }*/
    }

}