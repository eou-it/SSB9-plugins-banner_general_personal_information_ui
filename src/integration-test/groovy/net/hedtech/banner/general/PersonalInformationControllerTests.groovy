/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
********************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonalInformationControllerTests extends BaseIntegrationTestCase {

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new PersonalInformationController()
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
    void testCheckSecurityQuestionsExist() {
        loginSSB 'HOSS001', '111111'

        controller.request.contentType = "text/json"
        controller.checkSecurityQuestionsExist()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testCheckSecurityQuestionsDoNotExist() {
        loginSSB 'GDP000005', '111111'

        controller.request.contentType = "text/json"
        controller.checkSecurityQuestionsExist()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        assertNotNull data
        assertTrue data.failure
    }
}