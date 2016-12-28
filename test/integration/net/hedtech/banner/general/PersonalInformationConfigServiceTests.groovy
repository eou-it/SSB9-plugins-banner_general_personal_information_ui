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
        def val = personalInformationConfigService.getParamFromSession('PERS.INFO.UPDATE.PREF.EMAIL', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionWithPreexistingConfig() {
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): ['PERS.INFO.UPDATE.PREF.EMAIL': 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('PERS.INFO.UPDATE.PREF.EMAIL', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionWithPreexistingSessionConfigButNoPersonalInfoConfig() {
        def personConfigInSession = [dummy: [dummy: 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('PERS.INFO.UPDATE.PREF.EMAIL', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionBadKeyAndDefaultValue() {
        def val = personalInformationConfigService.getParamFromSession('I_DONT_EXIST', 'dummy_default_value')

        assertEquals "dummy_default_value", val
    }

    @Test
    void testGetParamFromSessionExcludedPropertyPERS_INFO_OVERVIEW_ADDRESS() {
        def val = personalInformationConfigService.getParamFromSession('PERS.INFO.OVERVIEW.ADDRESS', null)

        assertNull val
    }

    @Test
    void testGetParamFromSessionExcludedPropertyPERS_INFO_OVERVIEW_PHONE() {
        def val = personalInformationConfigService.getParamFromSession('PERS.INFO.OVERVIEW.PHONE', null)

        assertNull val
    }

    @Test
    void testGetPersonalInfoConfigFromSession() {
        def config = PersonalInformationConfigService.getPersonalInfoConfigFromSession()

        assertEquals "Y", config["PERS.INFO.UPDATE.PREF.EMAIL"]
    }

    @Test
    void testCreatePersonalInfoConfig() {
        def personConfig = [:]
        PersonalInformationConfigService.createPersonalInfoConfig(personConfig)
        def personalInfoConfig = personConfig[PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME]

        assertEquals "Y", personalInfoConfig["PERS.INFO.UPDATE.PREF.EMAIL"]
        assertEquals "Y", personalInfoConfig["PERS.INFO.DISP.PROFILE.PICTURE"]
        assertEquals "2", personalInfoConfig["PERS.INFO.EMAIL.SECTION.MODE"]
        assertEquals "2", personalInfoConfig["PERS.INFO.PHONE.SECTION.MODE"]
        assertEquals "2", personalInfoConfig["PERS.INFO.ADDRESS.SECTION.MODE"]
        assertEquals "2", personalInfoConfig["PERS.INFO.EMER.SECTION.MODE"]
        assertNull        personalInfoConfig["PERS.INFO.OVERVIEW.PHONE"]
        assertNull        personalInfoConfig["PERS.INFO.OVERVIEW.ADDRESS"]
    }
}
