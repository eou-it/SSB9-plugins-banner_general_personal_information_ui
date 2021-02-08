/*******************************************************************************
 Copyright 2021 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class AnswerSurveyControllerIntegrationTests extends BaseIntegrationTestCase {

    def answerSurveyCompositeService
    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        controller = new AnswerSurveyController()
        controller.answerSurveyCompositeService = answerSurveyCompositeService
        super.setUp()
    }

    /**
     * The tear down method will run after all test case method execution.
     */
    @After
    public void tearDown() {
        super.tearDown()
        super.logout()
    }


    @Test
    void testFetchSurveys() {
        loginSSB('ANSY-0003', '111111')
        controller.request.contentType = "text/json"
        controller.fetchSurveys()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        assertTrue data.success
        assertEquals(5, data.surveys.size())
    }

    @Test
    void testFetchSurveysNoSurveys() {
        loginSSB('ANSY-0001', '111111')
        controller.request.contentType = "text/json"
        controller.fetchSurveys()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        assertFalse data?.success
        assertEquals('No surveys are available at this time for your responses.', data?.error)
    }

    @Test
    void testFetchQuestionAnswers() {
        loginSSB('ANSY-0003', '111111')
        controller.request.contentType = "text/json"

        def inputJSON = [
                surveyName: 'REUNION',
                nextDisp  : '1'
        ]
        controller.params.putAll(inputJSON)
        controller.fetchQuestionAnswers()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        def result = data.questionDetails
        assertEquals('1', result[0].questionNo)
        assertEquals('Do you plan to attend?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(3, result[0].responseList.size())
        assertNull result[0].radioValue
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
        assertEquals('rsp11', result[0].responseList[1].name)
        assertEquals('2', result[0].responseList[1].value)
        assertEquals('No', result[0].responseList[1].responseText)
        assertEquals('N', result[0].allowComments)
    }

    @Test
    void testFetchQuestionAnswersWithSavedResponse() {
        loginSSB('ANSY-0003', '111111')
        controller.request.contentType = "text/json"
        def inputJSON = [
                surveyName: 'REUNION',
                nextDisp  : '3'
        ]
        controller.params.putAll(inputJSON)
        controller.fetchQuestionAnswers()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        def result = data.questionDetails
        assertEquals('3', result[0].questionNo)
        assertEquals('What mailing lists do wish to be on?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('Y', result[0].multiResponseInd)
        assertEquals(5, result[0].responseList.size())
        assertEquals(true, result[0].responseList[0].checked)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('Y', result[0].responseList[0].value)
        assertEquals('Friends of Music and Dance', result[0].responseList[0].responseText)
        assertEquals(true, result[0].responseList[4].checked)
        assertEquals('rsp15', result[0].responseList[4].name)
        assertEquals('Y', result[0].responseList[4].value)
        assertEquals('Friends of Art', result[0].responseList[4].responseText)
        assertEquals('Y', result[0].allowComments)
        assertEquals('cmnt1', result[0].commentName)
        assertEquals('Sample comment for Reunion Survey', result[0].comment)
    }

    @Test
    void testFetchQuestionAnswersWithComments() {
        loginSSB('ANSY-0003', '111111')
        controller.request.contentType = "text/json"
        def inputJSON = [
                surveyName: 'AGE',
                nextDisp  : '1'
        ]
        controller.params.putAll(inputJSON)
        controller.fetchQuestionAnswers()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        def result = data.questionDetails
        assertEquals('1', result[0].questionNo)
        assertEquals('What age are you at your earliest memories', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('Y', result[0].multiResponseInd)
        assertEquals(4, result[0].responseList.size())
        assertEquals(false, result[0].responseList[0].checked)
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('Y', result[0].responseList[0].value)
        assertEquals('I cant remember my childhood at all', result[0].responseList[0].responseText)
        assertEquals(false, result[0].responseList[1].checked)
        assertEquals('rsp12', result[0].responseList[1].name)
        assertEquals('Y', result[0].responseList[1].value)
        assertEquals('I remember everything from birth on', result[0].responseList[1].responseText)
        assertEquals('Y', result[0].allowComments)
    }

    @Test
    void testSaveResponsePreviousAction() {
        loginSSB('ANSY-0003', '111111')
        controller.request.contentType = "text/json"
        def inputJSON = [
                surveyName  : "REUNION",
                questionNo  : "3",
                nextDisp    : 2,
                responses   : [
                        [checked: false, name: "rsp11", value: "Y", responseText: "Friends of Music and Dance"],
                        [checked: false, name: "rsp12", value: "Y", responseText: "Friends of Athletics"],
                        [checked: false, name: "rsp13", value: "Y", responseText: "Scott Arboretum"],
                        [checked: false, name: "rsp14", value: "Y", responseText: "McCabe Library"],
                        [checked: true, name: "rsp15", value: "Y", responseText: "Friends of Art"]],
                comment     : "Sample comment for Reunion Survey",
                submitAction: "Previous"
        ]
        controller.request.setJSON(inputJSON)
        controller.saveResponse()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        def result = data.questionDetails
        assertEquals('2', result[0].questionNo)
        assertEquals('Is Swarthmore in your will?', result[0].questionText)
        assertEquals('qust1', result[0].questionCode)
        assertEquals('N', result[0].multiResponseInd)
        assertEquals(2, result[0].responseList.size())
        assertNull result[0].radioValue
        assertEquals('rsp11', result[0].responseList[0].name)
        assertEquals('1', result[0].responseList[0].value)
        assertEquals('Yes', result[0].responseList[0].responseText)
        assertEquals('rsp11', result[0].responseList[1].name)
        assertEquals('2', result[0].responseList[1].value)
        assertEquals('No', result[0].responseList[1].responseText)
        assertEquals('N', result[0].allowComments)
        assertNull result[0].commentName
        assertNull result[0].comment
    }

    @Test
    void testSaveResponseSurveyCompleteAction() {
        loginSSB('ANSY-0003', '111111')
        controller.request.contentType = "text/json"
        def inputJSON =  [
                    "surveyName":"REUNION",
                    "questionNo":"2","nextDisp":3,
                    "responses":[["checked":true,"name":"rsp11","value":"1","responseText":"Yes"],
                    ["checked":false,"name":"rsp11","value":2,"responseText":"No"]],
                    "submitAction":"Survey Complete"]
        controller.request.setJSON(inputJSON)
        controller.saveResponse()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        String message = data.message
        assertEquals('Thank you for completing the survey.', message)
    }
}