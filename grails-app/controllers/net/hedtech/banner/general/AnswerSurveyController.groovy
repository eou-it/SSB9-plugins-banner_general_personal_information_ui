/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON

class AnswerSurveyController {

    def answerSurveyCompositeService

    def fetchSurveys() {
        def result = answerSurveyCompositeService.fetchSurveys()
        render result as JSON
    }

    def fetchQuestionAnswers() {
        def outJson = answerSurveyCompositeService.fetchQuestionAnswers(params)
        render outJson as JSON
    }

    def saveResponse() {
        def inputJSON = request.JSON.toString()
        def outJson = answerSurveyCompositeService.saveResponse(inputJSON)
        render outJson as JSON
    }


}