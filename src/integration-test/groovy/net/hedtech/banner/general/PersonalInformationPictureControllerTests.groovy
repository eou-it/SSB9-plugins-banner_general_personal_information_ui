/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
********************************************************************************/

package net.hedtech.banner.general

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
import net.hedtech.banner.testing.BaseIntegrationTestCase

@Integration
@Rollback
class PersonalInformationPictureControllerTests extends BaseIntegrationTestCase {

    def controller

    public GrailsWebRequest mockRequest() {
        GrailsMockHttpServletRequest mockRequest = new GrailsMockHttpServletRequest();
        GrailsMockHttpServletResponse mockResponse = new GrailsMockHttpServletResponse();
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, mockRequest, mockResponse)
    }

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        webAppCtx = new GrailsWebApplicationContext()
        controller = Holders.grailsApplication.getMainContext().getBean("net.hedtech.banner.general.PersonalInformationPictureController")
        mockRequest()
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
    void testHasAccess() {
        mockRequest()
        SSBSetUp('GDP000005', '111111')

        def params = [ bannerId: "GDP000005"]

        controller.params.putAll(params)
        assertTrue controller.hasAccess()
    }

    @Test
    void testHasNoAccess() {
        mockRequest()
        SSBSetUp('GDP000005', '111111')

        def params = [ bannerId: "GDP000001"]

        controller.params.putAll(params)
        assertFalse controller.hasAccess()
    }

    @Test
    void testHasAccessNoId() {
        mockRequest()
        SSBSetUp('GDP000005', '111111')

        def params = [ bannerId: ""]

        controller.params.putAll(params)
        assertFalse controller.hasAccess()
    }
}