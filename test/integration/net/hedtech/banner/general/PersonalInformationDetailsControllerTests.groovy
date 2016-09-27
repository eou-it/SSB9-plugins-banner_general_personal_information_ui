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
    void testGetMaskingRules() {
        loginSSB 'GDP000005', '111111'

        controller.request.contentType = "text/json"
        controller.getMaskingRules()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
        assertFalse data.displayHouseNumber
        assertTrue data.displayStreetLine4
        assertFalse data.displayCountryCode
        assertTrue data.displayInternationalAccess
    }

    @Test
    void testGetAddressesForCurrentUser() {
        loginSSB 'GDP000005', '111111'

        controller.request.contentType = "text/json"
        controller.getAddresses()
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
        assertEquals 3, data.size()
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
        assertEquals 'Greece', data[0].nation
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
    void testDeleteAddress() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"
        controller.request.json = """{
            id:${addresses[0].id},
            version:${addresses[0].version}
        }""".toString()

        controller.deleteAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testGetEmails(){
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.getEmails()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertEquals 1, data.emails.size()
        assertEquals 'ansbates@telstra.com', data.emails[0].emailAddress
    }

    @Test
    void testGetEmailTypeList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 1, max: 10 ]

        controller.params.putAll(params)
        controller.getEmailTypeList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 10, data.size()
        assertEquals 'Family E-Mail', data[0].description
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
        assertEquals false, data.failure
    }

    @Test
    void testUpdateEmail() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def email = controller.personEmailService.getDisplayableEmails(pidm)[0]

        // update comment field
        controller.request.contentType = "text/json"
        controller.request.json = """{
            id:${email.id},
            version:${email.version},
            emailAddress:'ansbates@telstra.com',
            preferredIndicator:false,
            commentData:'welcome, world',
            displayWebIndicator:true,
            emailType:{
                code:'BUSI',
                description:'Business E-Mail'
            }
        }""".toString()

        controller.updateEmail()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data
        assertEquals false, data.failure
    }

    @Test
    void testDeleteEmail() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def email = controller.personEmailService.getDisplayableEmails(pidm)[0]

        controller.request.contentType = "text/json"
        controller.request.json = """{
            id:${email.id},
            version:${email.version},
            emailAddress:'ansbates@telstra.com',
            preferredIndicator:false,
            commentData:null,
            displayWebIndicator:true,
            emailType:{
                code:'BUSI',
                description:'Business E-Mail'
            }
        }""".toString()

        controller.deleteEmail()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testGetTelephoneNumbers() {
        loginSSB 'HOS00001', '111111'

        controller.request.contentType = "text/json"
        controller.getTelephoneNumbers()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data

        //TODO: uncomment when "No address exists..." issue is resolved.  This test code is not tested.
        //TODO: Be sure seed data contains an unlisted phone, as those should be returned as well.
//        def phones = data.telephones
//        assertEquals 2, phones.size()
//        assertEquals '610 4925555', phones[0].displayPhoneNumber
    }

    @Test
    void testGetTelephoneTypeList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getTelephoneTypeList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 10, data.size()
        assertEquals 'Administrative', data[0].description
    }

    @Test
    void testGetRelationshipList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getRelationshipList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 10, data.size()
        assertEquals 'Brother', data[1].description
    }

    @Test
    void testGetEmergencyContactsForCurrentUser() {
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.getEmergencyContacts()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
        assertEquals 1, data.emergencyContacts.size()
        assertEquals 'P', data.emergencyContacts[0].relationship.code
    }

    @Test
    void testAddEmergencyContact() {
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.request.json = """{
            priority: 2,
            lastName: 'Smith',
            firstName: 'Veronica',
            middleInitial: 'V',
            streetLine1: '123 Any Street',
            city: 'Anytown',
            zip: '77777',
            phoneArea: '610',
            phoneNumber: '555',
            phoneExtension: '1234',
            state:{
                code:'KY',
                description:'Kentucky'
            },
            relationship:{
                code:'P',
                description:'Spouse'
            }
        }""".toString()

        controller.addEmergencyContact()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data
        assertEquals false, data.failure
    }

    @Test
    void testUpdateEmergencyContact() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def contacts = controller.personEmergencyContactService.getEmergencyContactsByPidm(pidm)

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            id:${contacts[0].id},
            version:${contacts[0].version},
            priority: 1,
            lastName: 'Andersen',
            firstName: 'Ronald',
            streetLine1: '3391 Nuzum Court - UPDATED',
            city: 'Malvern',
            zip: '19355',
            phoneArea: '215',
            phoneNumber: '6336094',
            state:{
                code:'PA',
                description:'Pennsylvania'
            },
            relationship:{
                code:'P',
                description:'Spouse'
            }
        }""".toString()

        controller.updateEmergencyContact()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals false, data.failure
    }

    @Test
    void testUpdateEmergencyContactWithMissingId() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def contacts = controller.personEmergencyContactService.getEmergencyContactsByPidm(pidm)

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            version:${contacts[0].version},
            priority: 2,
            lastName: 'Smith',
            firstName: 'Veronica',
            middleInitial: 'V',
            streetLine1: '123 Any Street - UPDATED',
            city: 'Anytown',
            zip: '77777',
            phoneArea: '610',
            phoneNumber: '555',
            phoneExtension: '1234',
            state:{
                code:'KY',
                description:'Kentucky'
            },
            relationship:{
                code:'P',
                description:'Spouse'
            }
        }""".toString()

        controller.updateEmergencyContact()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data.message
        assertEquals true, data.failure
    }

    @Test
    void testUpdateEmergencyContactWithMissingVersion() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def contacts = controller.personEmergencyContactService.getEmergencyContactsByPidm(pidm)

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            id:${contacts[0].id},
            priority: 2,
            lastName: 'Smith',
            firstName: 'Veronica',
            middleInitial: 'V',
            streetLine1: '123 Any Street - UPDATED',
            city: 'Anytown',
            zip: '77777',
            phoneArea: '610',
            phoneNumber: '555',
            phoneExtension: '1234',
            state:{
                code:'KY',
                description:'Kentucky'
            },
            relationship:{
                code:'P',
                description:'Spouse'
            }
        }""".toString()

        controller.updateEmergencyContact()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        println data.message
        assertEquals true, data.failure
    }

}
