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
        def val = personalInformationConfigService.getParamFromSession('UPD_P_EMAL', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionWithPreexistingConfig() {
        def personConfigInSession = [(personalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): ['UPD_P_EMAL': 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('UPD_P_EMAL', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionWithPreexistingSessionConfigButNoPersonalInfoConfig() {
        def personConfigInSession = [dummy: [dummy: 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('UPD_P_EMAL', 'dummy_default_value')

        assertEquals "Y", val
    }

    @Test
    void testGetParamFromSessionBadKeyAndDefaultValue() {
        def val = personalInformationConfigService.getParamFromSession('I_DONT_EXIST', 'dummy_default_value')

        assertEquals "dummy_default_value", val
    }

    @Test
    void testGetPersonalInfoConfigFromSession() {
        def config = personalInformationConfigService.getPersonalInfoConfigFromSession()

        assertEquals "Y", config.UPD_P_EMAL
    }
}
