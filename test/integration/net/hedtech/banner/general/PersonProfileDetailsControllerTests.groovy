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
        controller.getActiveAddressesForCurrentUser()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
    }

    @Test
    void testGetCountyList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getCountyList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 10, data.size()
        assertEquals 'Ada County', data[0].description
    }

    @Test
    void testGetStateList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "p", offset: 1, max: 10 ]

        controller.params.putAll(params)
        controller.getStateList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 2, data.size()
        assertEquals 'Provence of Quebec', data[0].description
    }

    @Test
    void testGetNationList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 6, max: 10 ]

        controller.params.putAll(params)
        controller.getNationList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 10, data.size()
        assertEquals 'Haiti', data[0].nation
    }

}
