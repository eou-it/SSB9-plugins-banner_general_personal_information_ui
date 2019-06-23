/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
********************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
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
class PersonalInformationControllerTests extends BaseIntegrationTestCase {

   def controller
    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU','SELFSERVICE']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        controller = Holders.grailsApplication.getMainContext().getBean("net.hedtech.banner.general.PersonalInformationController")
    }

    /**
     * The tear down method will run after all test case method execution.
     */
    @After
    public void tearDown() {
        super.tearDown()
        super.logout()
    }

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }


    @Test
    void testCheckSecurityQuestionsExist() {
        mockRequest()
        SSBSetUp('HOSS001', '111111')
        controller.request.contentType = "text/json"
        controller.checkSecurityQuestionsExist()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testCheckSecurityQuestionsDoNotExist() {
        mockRequest()
        SSBSetUp('GDP000005', '111111')
        controller.request.contentType = "text/json"
        controller.checkSecurityQuestionsExist()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        assertTrue data.failure
    }
}