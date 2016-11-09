/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonalInformationDataServiceTests extends BaseIntegrationTestCase {

    def personalInformationDataService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testGetTelephones() {
        def pidm = PersonUtility.getPerson("510000001").pidm
        controller = new PersonalInformationDetailsController()

        def phoneNumbers = personalInformationDataService.getTelephones(pidm, controller.session)

        assertEquals 4, phoneNumbers.size()
        assertEquals '5555000', phoneNumbers[0].phoneNumber
        assertEquals '301 5555000 51', phoneNumbers[0].displayPhoneNumber
    }
}
