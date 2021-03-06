/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.overall.loginworkflow.PostLoginWorkflow
import net.hedtech.banner.security.ResetPasswordService
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException

class PersonalInformationController {
    static defaultAction = 'landingPage'

    def resetPasswordService


    def landingPage() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session.setAttribute("enableHRSecurityCheckForMedicalInfo", false)

        render model: [:], view: "personalInformation"
    }

    def raceAndEthnicitySurvey() {
        //Redirect back to landing page after the survey or the full profile in mobile
        def url = (params.mobile == 'y') ? '/ssb/personalInformation#/personalInformationMobileFullProfile' : '/ssb/personalInformation'

        request.getSession().setAttribute(PostLoginWorkflow.URI_ACCESSED, url)

        redirect uri: '/ssb/survey/survey'
    }

    /**
     * Check to ensure security questions are in order
     */
    def checkSecurityQuestionsExist () {
        def id = PersonalInformationControllerUtility.principalUsername
        Map questionsInfoMap

        try{
            questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(id)
        } catch (SQLException sqle) {
            def e = new ApplicationException('PersonalInformationController', sqle)
            render PersonalInformationControllerUtility.returnFailureMessage(e) as JSON
        }

        if (((List)questionsInfoMap.get(id)).size() == 0 || ((List)questionsInfoMap.get(id)).size() < questionsInfoMap.get(id+"qstn_no")) {
            render([failure: true, message: 'net.hedtech.banner.resetpasword.securityquestion.notfound.message'] as JSON)
        } else {
            render([failure: false] as JSON)
        }
    }

    def resetPasswordWithSecurityQuestions() {
        // Redirect back to landing page after reset
        SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl = '/ssb/personalInformation'

        session.setAttribute("requestPage", "questans")

        def id = PersonalInformationControllerUtility.principalUsername
        def params = [username: id]
        forward controller : "resetPassword", action: "questans", params : params
    }

}