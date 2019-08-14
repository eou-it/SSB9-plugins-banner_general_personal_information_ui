/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

import groovy.sql.Sql
import grails.util.Holders
import grails.web.context.ServletContextHolder
import net.hedtech.banner.general.overall.PinQuestion
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.web.servlet.context.GrailsWebApplicationContext
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest


@Integration
@Rollback
class PersonalInformationQAControllerIntegrationTests extends BaseIntegrationTestCase {

    def securityQAService
    def generalForStoringResponsesAndPinQuestionService

    def questionMinimumLength
    def answerMinimumLength

    def i_question1 = "Fav destination?"
    def i_question2 = "Fav food?"


    def controller

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    @Before
    public void setUp() {

        // For testing RESTful APIs, we don't want the default 'controller support' added by our base class.
        // Most importantly, we don't want to redefine the controller's params to be a map within this test,
        // as we need Grails to automatically populate the params from the request.
        //
        // So, we'll set the formContext and then call super(), just as if this were not a controller test.
        // That is, we'll set the controller after we call super() so the base class won't manipulate it.
        if (!isSsbEnabled()) return
        formContext = ['SELFSERVICE']
        webAppCtx = new GrailsWebApplicationContext()
        controller = Holders.grailsApplication.getMainContext().getBean("net.hedtech.banner.general.PersonalInformationQAController")

        super.setUp()
        ServletContextHolder.servletContext.removeAttribute("gtvsdax")
    }

    @After
    public void tearDown() {
        if (!isSsbEnabled()) return
        super.tearDown()
        logout()
    }


    private void setupUser(user){
        mockRequest()
        SSBSetUp(user, '111111')
    }

    @Test
    void testGetSecurityQA() {
        setupUser('HOSS001')
        controller.request.contentType = "text/json"
        controller.getSecurityQA()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)

        assertNotNull data
        /* commenting out the following asserts as they were conflicts when the banner core test SecurityQAServiceIntegrationTests
          was also running concurrently as that was adding more questions and affecting the table data.

        assertEquals 'Pet Name?', data.questions[0]
        assertEquals 'Y', data.userDefinedQuesFlag
        assertEquals 1, data.noOfquestions
        assertEquals 10, data.questionMinimumLength
        assertEquals 2, data.answerMinimumLength
        assertEquals 3, data.userQuestions.size()
        assertEquals 2, data.userQuestions[0].questionNum
        assertEquals 3, data.userQuestions[1].questionNum
        assertEquals 1, data.userQuestions[2].questionNum
        */
    }

    @Test
    void testUpdate(){
        setupUser('HOSS001')
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        if (!isSsbEnabled()) return

        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getUserDefinedPreference().GUBPPRF_NO_OF_QSTNS, 0

        def qstnList = generalForStoringResponsesAndPinQuestionService.fetchQuestionForPidm(pidm)

        controller.request.json = """{
            pin: "111111",
            questions:[
                {
                    id: ${qstnList[0].id},
                    version: ${qstnList[0].version},
                    question:"question2",
                    userDefinedQuestion:"",
                    answer:"answer1"
                },
                {
                    id: ${qstnList[1].id},
                    version: ${qstnList[1].version},
                    question:"question3",
                    userDefinedQuestion:"",
                    answer:"answer2"
                },
                {
                    id: ${qstnList[2].id},
                    version: ${qstnList[2].version},
                    question:"question0",
                    userDefinedQuestion:"This is a question?",
                    answer:"answer3"
                }
            ]
        }""".toString()

        controller.save()
        def data = JSON.parse(controller.response.contentAsString)
        int ansrCount = securityQAService.getNumberOfQuestionsAnswered(pidm)

        assertNotNull data
        assertTrue !data.failure
        assertEquals 3, ansrCount, 0
    }

    @Test
    void testSave(){
        setupUser('HOF00720')
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        if (!isSsbEnabled()) return
        def pinQuestion1  = newValidForCreatePinQuestion("PER1" ,i_question1 )
        pinQuestion1.save( failOnError: true, flush: true )
        assertNotNull pinQuestion1.id

        def pinQuestion2  = newValidForCreatePinQuestion("PER2",i_question2)
        pinQuestion2.save( failOnError: true, flush: true )
        assertNotNull pinQuestion2.id

        List questionList = []
        def ques = PinQuestion.fetchQuestions()
        Map questions = [:]
        ques.each {
            questions.put(it.pinQuestionId, it.description)
        }
        questionList = questions.values().collect()
        setNumberOfQuestion(3)
        assertEquals 3, securityQAService.getUserDefinedPreference().GUBPPRF_NO_OF_QSTNS, 0

        int question1Index = questionList.indexOf(pinQuestion1.getDescription())+1
        int question2Index = questionList.indexOf(pinQuestion2.getDescription()) +1
        String question1 = "question"+question1Index
        String question2 = "question"+question2Index

        controller.request.json = """{
            pin: "111111",
            questions:[
                {
                    question:${question1},
                    userDefinedQuestion:"",
                    answer:"answer1"
                },
                {
                    question:"question0",
                    userDefinedQuestion:"This is a question?",
                    answer:"answer2"
                },
                {
                    question:${question2},
                    userDefinedQuestion:"",
                    answer:"answer3"
                }
            ]
        }""".toString()

        controller.save()
        def data = JSON.parse(controller.response.contentAsString)
        int ansrCount = securityQAService.getNumberOfQuestionsAnswered(pidm)

        assertNotNull data
        assertTrue !data.failure
        assertEquals 3, ansrCount, 0
    }

    private void setNumberOfQuestion(int noOfQuestions){
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GUBPPRF set GUBPPRF_NO_OF_QSTNS = ?",[noOfQuestions])
        } finally {
          //TODO grails3  sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }

    private def newValidForCreatePinQuestion(String pinQuestionId,String desc) {
        def pinQuestion = new PinQuestion(
                pinQuestionId: pinQuestionId,
                description: desc,
                displayIndicator: true,
        )
        return pinQuestion
    }

    private def isSsbEnabled() {
        Holders.config.ssbEnabled instanceof Boolean ? Holders.config.ssbEnabled : false
    }
}
