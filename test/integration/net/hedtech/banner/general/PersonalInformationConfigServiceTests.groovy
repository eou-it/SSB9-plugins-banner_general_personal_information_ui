/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


class PersonalInformationConfigServiceTests extends BaseIntegrationTestCase {

    def personalInformationConfigService

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
    void testGetParamFromWebTailor() {
        def val = personalInformationConfigService.getParamFromWebTailor('SYSTEM_NAME', 'dummy_default_value')

        assertEquals "Banner", val
    }

    @Test
    void testGetParamFromWebTailorBadKeyAndDefaultValue() {
        def val = personalInformationConfigService.getParamFromWebTailor('I_DONT_EXIST', 'dummy_default_value')

        assertEquals "dummy_default_value", val
    }

    @Test
    void testSetPersonalInfoConfigInSession() {
        controller = new PersonalInformationDetailsController()
        assertNull controller.session.getAttribute(PersonalInformationConfigService.PI_CONFIG)

        personalInformationConfigService.setPersonalInfoConfigInSession(controller.session, [:])

        assertNotNull controller.session.getAttribute(PersonalInformationConfigService.PI_CONFIG)
    }

    @Test
    void testGetPersonalInfoConfigInSession() {
        controller = new PersonalInformationDetailsController()
        assertNull personalInformationConfigService.getPersonalInfoConfigFromSession(controller.session)

        controller.session.setAttribute(PersonalInformationConfigService.PI_CONFIG, [:])

        assertNotNull personalInformationConfigService.getPersonalInfoConfigFromSession(controller.session)
    }

    @Test
    void testGetAddressDisplayPriorities() {
        controller = new PersonalInformationDetailsController()
        assertNull controller.session.getAttribute(PersonalInformationConfigService.PI_CONFIG)

        def addrPriorities = personalInformationConfigService.getAddressDisplayPriorities(controller.session)

        assertNotNull controller.session.getAttribute(PersonalInformationConfigService.PI_CONFIG)

        // Note that in seed data there are two GTVSDAX records for address priorities.  They both have an
        // internal code (GTVSDAX_INTERNAL_CODE) of PINFOADDRESS and internal code group (GTVSDAX_INTERNAL_CODE_GROUP)
        // of ADDRESS, which are the two fields used to query address priority, and one has a sequence number of 1 and
        // the other 2.  We only see the "2" one here because they're stored in the piConfig object in a hashmap, so
        // only the latest one stored (2) remains after the piConfig object is created.
        assertEquals(2, addrPriorities.UPDATE_ME)
    }
}
