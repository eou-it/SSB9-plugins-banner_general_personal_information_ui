/******************************************************************************
Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.general

import grails.converters.JSON
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonUtility
import org.codehaus.groovy.grails.web.json.JSONObject
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
    void testGetAddressesForCurrentUserButHidden() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit access to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [
                (PersonalInformationConfigService.ADDR_MODE): '0',
                (PersonalInformationConfigService.DISPLAY_OVERVIEW_ADDR): 'N',
        ]]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.request.contentType = "text/json"
        controller.getAddresses()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
        assertTrue data.failure
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
        assertEquals 'Abbeville, SC', data[0].description
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
        assertEquals 4, data.size()
        assertEquals 'Prince Edward Island', data[0].description
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
        assertEquals 'Burkina Faso', data[0].nation
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
        assertTrue 7 <= data.size()
        assertTrue 10 >= data.size()
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
                code:"UT",
                description:"Utah"
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
        assertFalse data.failure
    }

    @Test
    void testAddAddressWithConfigSetToNotUpdateable() {
        loginSSB 'HOSH00018', '111111'

        // Set configuration to prohibit updates to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.ADDR_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

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
        assertTrue data.failure
    }

    @Test
    void testUpdateAddress() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressByRoleViewService.getActiveAddressesByRoles(controller.getRoles(), pidm)
        int index
        if(addresses[0].addressType == 'PR') {
            index = 0
        }
        else{
            index = 1
        }

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            id:${addresses[index].id},
            version:${addresses[index].version},
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
        println data
        println addresses
        assertFalse data.failure
    }

    @Test
    void testUpdateAddressWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.ADDR_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressByRoleViewService.getActiveAddressesByRoles(controller.getRoles(), pidm)
        int index
        if(addresses[0].addressType == 'PR') {
            index = 0
        }
        else{
            index = 1
        }

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """{
            id:${addresses[index].id},
            version:${addresses[index].version},
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
        println data
        println addresses
        assertTrue data.failure
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
        assertTrue data.failure
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
        assertTrue data.failure
    }

    @Test
    void testDeleteAddress() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"
        controller.request.json = """{
            id:${addresses[0].id},
            version:${addresses[0].version},
            addressType: {
                code:${addresses[0].addressType.code}
            }
        }""".toString()

        controller.deleteAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testDeleteAddressWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.ADDR_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def addresses = controller.personAddressService.getActiveAddresses([pidm: pidm]).list

        controller.request.contentType = "text/json"
        controller.request.json = """{
            id:${addresses[0].id},
            version:${addresses[0].version},
            addressType: {
                code:${addresses[0].addressType.code}
            }
        }""".toString()

        controller.deleteAddress()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testGetEmails(){
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.getEmails()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertEquals 2, data.emails.size()
        assertEquals 'ansbates@telstra.com', data.emails[0].emailAddress
        assertTrue data.isPreferredEmailVisible
    }

    @Test
    void testGetEmailsButHidden(){
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit access to emails
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [
                (PersonalInformationConfigService.EMAIL_MODE): '0',
                (PersonalInformationConfigService.DISPLAY_OVERVIEW_EMAIL): 'N'
        ]]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.request.contentType = "text/json"
        controller.getEmails()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testGetEmailsPreferredInvisible() {
        loginSSB 'GDP000003', '111111'

        controller.request.contentType = "text/json"
        controller.getEmails()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertEquals 1, data.emails.size()
        assertEquals 'batescry@theweb.net', data.emails[0].emailAddress
        assertFalse data.isPreferredEmailVisible
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
        assertEquals 'Denver Email', data[0].description
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
        assertFalse data.failure
    }

    @Test
    void testAddEmailWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to email
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMAIL_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

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
        assertTrue data.failure
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
    void testAddEmailReactivate() {
        loginSSB 'GDP000001', '111111'

        // this user's below email should be inactive
        controller.request.contentType = "text/json"
        controller.request.json = """{
            emailAddress:'mrbigbear@theweb.com',
            preferredIndicator:false,
            commentData: null,
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
        assertFalse data.failure
    }

    @Test
    void testUpdateEmailWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to email
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMAIL_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

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
        assertTrue data.failure
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
    void testDeleteEmailWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to email
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMAIL_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

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
        assertTrue data.failure
    }

    @Test
    void testGetTelephoneNumbers() {
        loginSSB 'GDP000005', '111111'

        controller.request.contentType = "text/json"
        controller.getTelephoneNumbers()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data

        def phones = data.telephones
        assertEquals 1, phones.size()
        assertEquals '215 2083094', phones[0].displayPhoneNumber
    }

    @Test
    void testGetTelephoneNumbersButHidden() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit access to telephone numbers
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [
                (PersonalInformationConfigService.PHONE_MODE): '0',
                (PersonalInformationConfigService.DISPLAY_OVERVIEW_PHONE): 'N'
        ]]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.request.contentType = "text/json"
        controller.getTelephoneNumbers()

        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )
        assertNotNull data
        assertTrue data.failure
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
    void testAddTelephoneNumber() {
        loginSSB 'GDP000001', '111111'

        controller.request.contentType = "text/json"
        controller.request.json = """{
            telephoneType: {
              code: "PAGE",
              description: "Pager",
            },
            internationalAccess: null,
            countryPhone: null,
            phoneArea: "215",
            phoneNumber: "2083094",
            phoneExtension: null,
            unlistIndicator: null,
        }""".toString()

        controller.addTelephoneNumber()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testAddTelephoneNumberWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to phone number
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.PHONE_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.request.contentType = "text/json"
        controller.request.json = """{
            telephoneType: {
              code: "PAGE",
              description: "Pager",
            },
            internationalAccess: null,
            countryPhone: null,
            phoneArea: "215",
            phoneNumber: "2083094",
            phoneExtension: null,
            unlistIndicator: null,
        }""".toString()

        controller.addTelephoneNumber()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdateTelephoneNumber() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def phone = controller.personTelephoneService.fetchActiveTelephonesByPidm(pidm)[0]

        //update phoneNumber
        controller.request.contentType = "text/json"
        controller.request.json = """{
            id: ${phone.id},
            version: ${phone.version},
            telephoneType: {
              code: "PR",
              description: "Permanent",
            },
            internationalAccess: null,
            countryPhone: null,
            phoneArea: "215",
            phoneNumber: "8675309",
            phoneExtension: null,
            unlistIndicator: null,
        }""".toString()

        controller.updateTelephoneNumber()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testUpdateTelephoneNumberWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to phone number
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.PHONE_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def phone = controller.personTelephoneService.fetchActiveTelephonesByPidm(pidm)[0]

        //update phoneNumber
        controller.request.contentType = "text/json"
        controller.request.json = """{
            id: ${phone.id},
            version: ${phone.version},
            telephoneType: {
              code: "PR",
              description: "Permanent",
            },
            internationalAccess: null,
            countryPhone: null,
            phoneArea: "215",
            phoneNumber: "8675309",
            phoneExtension: null,
            unlistIndicator: null,
        }""".toString()

        controller.updateTelephoneNumber()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testDeleteTelephoneNumber() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def phone = controller.personTelephoneService.fetchActiveTelephonesByPidm(pidm)[0]

        controller.request.contentType = "text/json"
        controller.request.json = """{
            id: ${phone.id},
            version: ${phone.version},
            telephoneType: {
              code: "PR",
              description: "Permanent",
            },
            internationalAccess: null,
            countryPhone: null,
            phoneArea: "215",
            phoneNumber: "2083094",
            phoneExtension: null,
            unlistIndicator: null,
        }""".toString()

        controller.deleteTelephoneNumber()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testDeleteTelephoneNumberWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to phone number
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.PHONE_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def phone = controller.personTelephoneService.fetchActiveTelephonesByPidm(pidm)[0]

        controller.request.contentType = "text/json"
        controller.request.json = """{
            id: ${phone.id},
            version: ${phone.version},
            telephoneType: {
              code: "PR",
              description: "Permanent",
            },
            internationalAccess: null,
            countryPhone: null,
            phoneArea: "215",
            phoneNumber: "2083094",
            phoneExtension: null,
            unlistIndicator: null,
        }""".toString()

        controller.deleteTelephoneNumber()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
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
    void testGetEmergencyContactsForCurrentUserButHidden() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMER_MODE): '0']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.request.contentType = "text/json"
        controller.getEmergencyContacts()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse(dataForNullCheck)
        println data
        assertNotNull data
        assertTrue data.failure
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
        assertFalse data.failure
    }

    @Test
    void testAddEmergencyContactWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to phone number
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMER_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

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
        assertTrue data.failure
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
        assertFalse data.failure
    }

    @Test
    void testUpdateEmergencyContactWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to phone number
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMER_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

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
        assertTrue data.failure
    }

    @Test
    void testDeleteEmergencyContact() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def contacts = controller.personEmergencyContactService.getEmergencyContactsByPidm(pidm)

        controller.request.contentType = "text/json"

        controller.request.json = """{
            id:${contacts[0].id},
            version:${contacts[0].version},
            priority: 1,
            lastName: 'Andersen',
            firstName: 'Ronald',
            streetLine1: '3391 Nuzum Court',
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

        controller.deleteEmergencyContact()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertNotNull data.emergencyContacts
        assertEquals 0, data.emergencyContacts.size()

    }

    @Test
    void testDeleteEmergencyContactWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to phone number
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.EMER_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def contacts = controller.personEmergencyContactService.getEmergencyContactsByPidm(pidm)

        controller.request.contentType = "text/json"

        controller.request.json = """{
            id:${contacts[0].id},
            version:${contacts[0].version},
            priority: 1,
            lastName: 'Andersen',
            firstName: 'Ronald',
            streetLine1: '3391 Nuzum Court',
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

        controller.deleteEmergencyContact()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testGetPreferredName() {
        loginSSB 'HOSH00018', '111111'

        controller.getPreferredName()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals '2Curr2ProgSameStudyPath EBRUSER1', data.preferredName
    }

    @Test
    void testGetUserName() {
        loginSSB 'HOSH00018', '111111'

        controller.getUserName()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals '2Curr2ProgSameStudyPath', data.firstName
        assertEquals JSONObject.NULL, data.middleName
        assertEquals 'EBRUSER1', data.lastName
    }

    @Test
    void testGetBannerId() {
        loginSSB 'HOSH00018', '111111'

        controller.getBannerId()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 'HOSH00018', data.bannerId
    }

    @Test
    void testGetMaritalStatusList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getMaritalStatusList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue 10 > data.size()
        assertTrue data.description.contains('Divorced')
    }

    @Test
    void testGetGenderList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getGenderList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue 3 <= data.size()
        assertTrue data.description.contains('Woman')
    }

    @Test
    void testGetPronounList() {
        loginSSB 'HOSH00018', '111111'

        def params = [ searchString: "", offset: 0, max: 10 ]

        controller.params.putAll(params)
        controller.getPronounList()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue 3 <= data.size()
        assertTrue data.description.contains('one')
    }

    @Test
    void testGetPersonalDetails() {
        loginSSB 'GDP000005', '111111'

        controller.getPersonalDetails()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals '03/31/1961', data.birthDate
        assertEquals 'F', data.sex
        assertEquals JSONObject.NULL, data.preferenceFirstName
        assertEquals 'M', data.maritalStatus.code
        assertEquals '1', data.ethnic
    }

    @Test
    void testGetPersonalDetailsButHidden() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit access to personal details
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [
                (PersonalInformationConfigService.PERS_DETAILS_MODE): '0',
                (PersonalInformationConfigService.VETERANS_CLASSIFICATION): 'N'
        ]]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.getPersonalDetails()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdatePersonalDetails() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def details = controller.personBasicPersonBaseService.getPersonalDetailsForPersonalInformation(pidm)

        controller.request.contentType = "text/json"

        // Updating preferred first name and marital status
        controller.request.json = """{
            id:${details.id},
            version: ${details.version},
            preferenceFirstName: 'NickName',
            maritalStatus:{
                code:'S',
                description:'Single'
            },
            gender: {
                code: null,
                description: null
            },
            pronoun: {
                code: B002,
                description: 'she'
            }
        }""".toString()

        controller.updatePersonalDetails()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testUpdatePersonalDetailsWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to Personal Details
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.PERS_DETAILS_MODE): '1']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def details = controller.personBasicPersonBaseService.getPersonalDetailsForPersonalInformation(pidm)

        controller.request.contentType = "text/json"

        // Updating preferred first name and marital status
        controller.request.json = """{
            id:${details.id},
            version: ${details.version},
            preferenceFirstName: 'NickName',
            maritalStatus:{
                code:'S',
                description:'Single'
            }
        }""".toString()

        controller.updatePersonalDetails()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdatePersonalDetailsWithGndrConfigSetToOff() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to Personal Details
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.GENDER_PRONOUN): 'N']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def details = controller.personBasicPersonBaseService.getPersonalDetailsForPersonalInformation(pidm)

        controller.request.contentType = "text/json"

        // Updating preferred first name and marital status, gender should have no effect
        controller.request.json = """{
            id:${details.id},
            version: ${details.version},
            preferenceFirstName: 'NickName',
            maritalStatus:{
                code:'S',
                description:'Single'
            },
            gender:{
               code: 'INVALID_CODE',
               description: 'should not matter'
            }
        }""".toString()

        controller.updatePersonalDetails()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testGetRaces() {
        loginSSB 'GDP000005', '111111'

        controller.getRaces()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 'ASI', data.races[0].race
        assertEquals 'Asian', data.races[0].description
    }

    @Test
    void testGetPiConfig() {
        loginSSB 'HOSH00018', '111111'

        controller.getPiConfig()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.isPreferredEmailUpdateable
        assertTrue data.isProfilePicDisplayable
        assertTrue data.isOverviewAddressDisplayable
        assertTrue data.isOverviewPhoneDisplayable
        assertTrue data.isOverviewEmailDisplayable
        assertTrue data.isDirectoryProfileDisplayable
        assertTrue data.isVetClassificationDisplayable
        assertTrue data.isSecurityQandADisplayable
        assertTrue data.isPasswordChangeDisplayable
        assertTrue data.isDisabilityStatusDisplayable
        assertTrue data.isMaritalStatusUpdateable
        assertTrue data.additionalDetailsSectionMode
        assertTrue data.otherSectionMode
        assertTrue data.isGenderPronounDisplayable
        assertEquals '2',data.emailSectionMode
        assertEquals '2',data.telephoneSectionMode
        assertEquals '2',data.addressSectionMode
        assertEquals '2',data.personalDetailsSectionMode
        assertEquals '2',data.emergencyContactSectionMode
    }

    @Test
    void testGetPiConfigSecurityQuestionDisabled() {
        loginSSB 'HOSH00018', '111111'

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GUBPPRF set GUBPPRF_NO_OF_QSTNS = ?",[0])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }

        controller.getPiConfig()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.isPreferredEmailUpdateable
        assertTrue data.isProfilePicDisplayable
        assertTrue data.isOverviewAddressDisplayable
        assertTrue data.isOverviewPhoneDisplayable
        assertTrue data.isOverviewEmailDisplayable
        assertTrue data.isDirectoryProfileDisplayable
        assertTrue data.isVetClassificationDisplayable
        assertFalse data.isSecurityQandADisplayable
        assertTrue data.isPasswordChangeDisplayable
        assertTrue data.isDisabilityStatusDisplayable
        assertTrue data.isMaritalStatusUpdateable
        assertTrue data.additionalDetailsSectionMode
        assertTrue data.otherSectionMode
        assertTrue data.isGenderPronounDisplayable
        assertEquals '2',data.emailSectionMode
        assertEquals '2',data.telephoneSectionMode
        assertEquals '2',data.addressSectionMode
        assertEquals '2',data.personalDetailsSectionMode
        assertEquals '2',data.emergencyContactSectionMode
    }

    @Test
    void testGetDirectoryProfile() {
        loginSSB 'GDP000005', '111111'

        controller.getDirectoryProfile()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
    }

    @Test
    void testGetDirectoryProfileButHidden() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.DIRECTORY_PROFILE): 'N']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.getDirectoryProfile()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdateDirectoryProfilePreferences() {
        loginSSB 'GDP000001', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """[
            {
                code: 'ADDR_PR',
                checked: true
            },
            {
                code: 'EMAIL',
                checked: true
            }
        ]""".toString()

        controller.updateDirectoryProfilePreferences()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testUpdateDirectoryProfilePreferencesWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000001', '111111'

        // Set configuration to prohibit updates to Directory Profile
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.DIRECTORY_PROFILE): 'N']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        controller.request.contentType = "text/json"

        // Updating streetLine1
        controller.request.json = """[
            {
                code: 'ADDR_PR',
                checked: true
            },
            {
                code: 'EMAIL',
                checked: true
            }
        ]""".toString()

        controller.updateDirectoryProfilePreferences()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testGetDisabilityStatus() {
        loginSSB 'GDP000005', '111111'

        controller.getDisabilityStatus()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertEquals 0, data.disabilityStatusCode
    }

    @Test
    void testGetDisabilityStatusButHidden() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to address
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.DISABILITY_STATUS): 'N']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.getDisabilityStatus()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdateDisabilityStatus() {
        loginSSB 'HOSH00018', '111111'

        controller.request.contentType = "text/json"

        controller.request.json = """{
                code:'DN'
        }""".toString()

        controller.updateDisabilityStatus()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testUpdateDisabilityStatusWithConfigSetToNotUpdateable() {
        loginSSB 'HOSH00018', '111111'

        // Set configuration to prohibit updates to Disability Status
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.DISABILITY_STATUS): 'N']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        controller.request.contentType = "text/json"

        controller.request.json = """{
                code:'DN'
        }""".toString()

        controller.updateDisabilityStatus()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdateVeteranClassification() {
        loginSSB 'GDP000005', '111111'

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def details = controller.personBasicPersonBaseService.getPersonalDetailsForPersonalInformation(pidm)

        controller.request.contentType = "text/json"

        controller.request.json = """{
            id:${details.id},
            version: ${details.version},
            veraIndicator: 'B',
            sdvetIndicator: 'Y',
            activeDutySeprDate: null,
            armedServiceMedalVetIndicator: false
        }""".toString()

        controller.updateVeteranClassification()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertFalse data.failure
    }

    @Test
    void testUpdateVeteranClassificationNoPersRecord() {
        loginSSB 'HOSH00018', '111111'

        controller.request.contentType = "text/json"

        controller.request.json = """{
            id: null,
            version: null,
            veraIndicator: 'B',
            sdvetIndicator: 'Y',
            activeDutySeprDate: null,
            armedServiceMedalVetIndicator: false
        }""".toString()

        controller.updateVeteranClassification()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }

    @Test
    void testUpdateVeteranClassificationWithConfigSetToNotUpdateable() {
        loginSSB 'GDP000005', '111111'

        // Set configuration to prohibit updates to Disability Status
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): [(PersonalInformationConfigService.VETERANS_CLASSIFICATION): 'N']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def details = controller.personBasicPersonBaseService.getPersonalDetailsForPersonalInformation(pidm)

        controller.request.contentType = "text/json"

        controller.request.json = """{
            id:${details.id},
            version: ${details.version},
            veraIndicator: 'B',
            sdvetIndicator: 'Y',
            activeDutySeprDate: null,
            armedServiceMedalVetIndicator: false
        }""".toString()

        controller.updateVeteranClassification()
        def dataForNullCheck = controller.response.contentAsString
        def data = JSON.parse( dataForNullCheck )

        assertNotNull data
        assertTrue data.failure
    }
}
