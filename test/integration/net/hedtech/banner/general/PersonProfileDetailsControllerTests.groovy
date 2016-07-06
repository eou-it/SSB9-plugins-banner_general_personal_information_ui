package net.hedtech.banner.general

import grails.converters.JSON
import org.junit.After
import org.junit.Before
import org.junit.Test

import net.hedtech.banner.testing.BaseIntegrationTestCase

class PersonProfileDetailsControllerTests extends BaseIntegrationTestCase {

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new PersonProfileDetailsController()
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
    void testGetAddressesForCurrentUser(){
        loginSSB 'MYE000001', '111111'

        controller.request.contentType = "text/json"
        controller.getAddressesForCurrentUser()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
    }

}
