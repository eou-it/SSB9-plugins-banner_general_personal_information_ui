/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import net.hedtech.banner.overall.loginworkflow.PostLoginWorkflow

class PersonalInformationController {
    static defaultAction = 'landingPage'

    def landingPage() {
        render model: [:], view: "personalInformation"
    }

    def raceAndEthnicitySurvey() {
        //Redirect back to landing page after the survey
        request.getSession().setAttribute(PostLoginWorkflow.URI_ACCESSED, '/ssb/personalInformation')

        redirect uri: '/ssb/survey/survey'
    }

}