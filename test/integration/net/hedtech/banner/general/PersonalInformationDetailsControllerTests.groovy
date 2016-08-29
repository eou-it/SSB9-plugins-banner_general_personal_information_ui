package net.hedtech.banner.general

import grails.converters.JSON
import org.junit.After
import org.junit.Before
import org.junit.Test

import net.hedtech.banner.testing.BaseIntegrationTestCase

class PersonalInformationDetailsControllerTests extends BaseIntegrationTestCase {

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new PersonalInformationDetailsController()
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
        loginSSB 'GDP000005', '111111'

        controller.request.contentType = "text/json"
        controller.getActiveAddressesForCurrentUser()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
        assertEquals 2, data.addresses.size()
        assertEquals 'OT', data.addresses[0].addressType.code
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

    @Test
    void testGetAddressTypeList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getAddressTypeList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 7, data.size()
        assertEquals 'Foreign', data[0].description
    }

    @Test
    void testAddAddress() {
        loginSSB 'HOSH00018', '111111'

        controller.request.contentType = "text/json"
        controller.request.json = """{
            addressType:{
                code:"MA",
                description:"Mailing"
            },
            city:"SomeCity",
            county:{
                code:"261",
                description:"Kittitas County"
            },
            fromDate:"2016-07-07T01:11:00.000Z",
            houseNumber:null,
            nation:{
                code:"157",
                description:"United States of America"
            },
            state:{
                code:"CM",
                description:"Northern Mariana Islands"
            },
            streetLine1:"123 Fake Street",
            streetLine2:"Apt 333",
            streetLine3:null,
            streetLine4:null,
            toDate:null,
            zip:"101112"
        }""".toString()

        controller.addAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals false, data.failure
    }

    @Test
    void testUpdateAddress() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            id:${addresses[0].id},
            version:${addresses[0].version},
            addressType:{
                code:"PR",
                description:"Permanent"
            },
            city:"Malvern",
            county:null,
            fromDate:"2034-01-01T05:00:00.000Z",
            houseNumber:"HN 1",
            nation:null,
            state:{
                code:"PA",
                description:"Pennsylvania"
            },
            streetLine1:"435 UPDATED Avenue",
            streetLine2:null,
            streetLine3:null,
            streetLine4:null,
            toDate:null,
            zip:"19355"
        }""".toString()

        controller.updateAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals false, data.failure
    }

    @Test
    void testUpdateAddressWithMissingId() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            version:${addresses[0].version},
            addressType:{
                code:"PR",
                description:"Permanent"
            },
            city:"Malvern",
            county:null,
            fromDate:"2034-01-01T05:00:00.000Z",
            houseNumber:"HN 1",
            nation:null,
            state:{
                code:"PA",
                description:"Pennsylvania"
            },
            streetLine1:"435 UPDATED Avenue",
            streetLine2:null,
            streetLine3:null,
            streetLine4:null,
            toDate:null,
            zip:"19355"
        }""".toString()

        controller.updateAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data.message
        assertEquals true, data.failure
    }

    @Test
    void testUpdateAddressWithMissingVersion() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            id:${addresses[0].id},
            addressType:{
                code:"PR",
                description:"Permanent"
            },
            city:"Malvern",
            county:null,
            fromDate:"2009-01-01T05:00:00.000Z",
            houseNumber:"HN 1",
            nation:null,
            state:{
                code:"PA",
                description:"Pennsylvania"
            },
            streetLine1:"435 UPDATED Avenue",
            streetLine2:null,
            streetLine3:null,
            streetLine4:null,
            toDate:null,
            zip:"19355"
        }""".toString()

        controller.updateAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data.message
        assertEquals true, data.failure
    }

    @Test
    void testAddInvalidAddress() {
        loginSSB 'HOSH00018', '111111'

        // Invalid streetLine1; should not be null
        controller.request.contentType = "text/json"
        controller.request.json = """{
            addressType:{
                code:"MA",
                description:"Mailing"
            },
            city:"SomeCity",
            county:{
                code:"261",
                description:"Kittitas County"
            },
            fromDate:"2016-07-07T01:11:00.000Z",
            houseNumber:null,
            nation:{
                code:"157",
                description:"United States of America"
            },
            state:{
                code:"CM",
                description:"Northern Mariana Islands"
            },
            streetLine1:null,
            streetLine2:"Apt 333",
            streetLine3:null,
            streetLine4:null,
            toDate:null,
            zip:"101112"
        }""".toString()

        controller.addAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data.message
        assertEquals true, data.failure
    }

    @Test
    void testDeleteAddresses() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"
        controller.request.json = """[{
            id:${addresses[0].id}
        }]""".toString()

        controller.deleteAddresses()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testFetchEmails(){
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.fetchEmails()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertEquals 1, data.size()
        assertEquals 'ansbates@telstra.com', data[0].emailAddress
    }

    @Test
    void testAddEmail() {
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.request.json = """{
            emailAddress:'myemail@somesite.org',
            preferredIndicator: false,
            commentData:null,
            emailType:{
                code:'PERS',
                description:'Personal Email'
            }
        }""".toString()

        controller.addEmail()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data
        assertEquals false, data.failure
    }

    @Test
    void testAddDupeEmail() {
        loginSSB 'GDP000001', '111111'

        // this email record should already exist
        controller.request.contentType = "text/json"
        controller.request.json = """{
            emailAddress:'ansbates@telstra.com',
            preferredIndicator:false,
            commentData:'hello world',
            emailType:{
                code:'BUSI',
                description:'Business E-Mail'
            }
        }""".toString()

        controller.addEmail()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data
        assertEquals true, data.failure
    }

    @Test
    void testAdd2ndPreferredEmail() {
        loginSSB 'GDP000001', '111111'

        // this user should already have a preferred email selected
        controller.request.contentType = "text/json"
        controller.request.json = """{
            emailAddress:'ansbates@telstra.com',
            preferredIndicator:true,
            commentData:'hello world',
            emailType:{
                code:'PWEB',
                description:'Personal Web Page'
            }
        }""".toString()

        controller.addEmail()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data
        assertEquals true, data.failure
    }

}
