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
    void testGetParamFromSessionWithNoPreexistingConfig() {
        def val = personalInformationConfigService.getParamFromSession('PREFERRED.EMAIL.UPDATABILITY', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionWithPreexistingConfig() {
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): ['PREFERRED.EMAIL.UPDATABILITY': 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('PREFERRED.EMAIL.UPDATABILITY', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionWithPreexistingSessionConfigButNoPersonalInfoConfig() {
        def personConfigInSession = [dummy: [dummy: 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('PREFERRED.EMAIL.UPDATABILITY', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionBadKeyAndDefaultValue() {
        def val = personalInformationConfigService.getParamFromSession('I_DONT_EXIST', 'dummy_default_value')

        assertEquals "dummy_default_value", val
    }

    @Test
    void testGetParamFromSessionExcludedPropertyPERS_INFO_OVERVIEW_ADDRESS() {
        def val = personalInformationConfigService.getParamFromSession('OVERVIEW.ADDRESS', null)

        assertNull val
    }

    @Test
    void testGetParamFromSessionExcludedPropertyPERS_INFO_OVERVIEW_PHONE() {
        def val = personalInformationConfigService.getParamFromSession('OVERVIEW.PHONE', null)

        assertNull val
    }

    @Test
    void testGetPersonalInfoConfigFromSession() {
        def config = PersonalInformationConfigService.getPersonalInfoConfigFromSession()

        assertEquals "Y", config["PREFERRED.EMAIL.UPDATABILITY"]
    }

    @Test
    void testCreatePersonalInfoConfig() {
        def personConfig = [:]
        PersonalInformationConfigService.createPersonalInfoConfig(personConfig)
        def personalInfoConfig = personConfig[PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME]

        assertEquals "Y", personalInfoConfig["PREFERRED.EMAIL.UPDATABILITY"]
        assertEquals "Y", personalInfoConfig["DISPLAY.PROFILE.PICTURE"]
        assertEquals "2", personalInfoConfig[PersonalInformationConfigService.EMAIL_MODE]
        assertEquals "2", personalInfoConfig[PersonalInformationConfigService.PHONE_MODE]
        assertEquals "2", personalInfoConfig[PersonalInformationConfigService.ADDR_MODE]
        assertEquals "2", personalInfoConfig[PersonalInformationConfigService.EMER_MODE]
        assertNull        personalInfoConfig["OVERVIEW.PHONE"]
        assertNull        personalInfoConfig["OVERVIEW.ADDRESS"]
    }
}
