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

        assertEquals PersonalInformationConfigService.YES, val
    }

    @Test
    void testGetParamFromSessionWithPreexistingConfig() {
        def personConfigInSession = [(PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME): ['PREFERRED.EMAIL.UPDATABILITY': 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('PREFERRED.EMAIL.UPDATABILITY', 'dummy_default_value')

        assertEquals PersonalInformationConfigService.YES, val
    }

    @Test
    void testGetParamFromSessionWithPreexistingSessionConfigButNoPersonalInfoConfig() {
        def personConfigInSession = [dummy: [dummy: 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def val = personalInformationConfigService.getParamFromSession('PREFERRED.EMAIL.UPDATABILITY', 'dummy_default_value')

        assertEquals PersonalInformationConfigService.YES, val
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

        assertEquals PersonalInformationConfigService.YES, config["PREFERRED.EMAIL.UPDATABILITY"]
    }

    @Test
    void testCreatePersonalInfoConfig() {
        def personConfig = [:]
        PersonalInformationConfigService.createPersonalInfoConfig(personConfig)
        def personalInfoConfig = personConfig[PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME]

        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.PREF_EMAIL]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.PROFILE_PICTURE]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.DISPLAY_OVERVIEW_ADDR]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.DISPLAY_OVERVIEW_PHONE]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.DISPLAY_OVERVIEW_EMAIL]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.DIRECTORY_PROFILE]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.DISABILITY_STATUS]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.PASSWORD_CHANGE]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.SECURITY_QA_CHANGE]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.VETERANS_CLASSIFICATION]
        assertEquals PersonalInformationConfigService.YES, personalInfoConfig[PersonalInformationConfigService.MARITAL_STATUS]
        assertEquals PersonalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig[PersonalInformationConfigService.EMAIL_MODE]
        assertEquals PersonalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig[PersonalInformationConfigService.PHONE_MODE]
        assertEquals PersonalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig[PersonalInformationConfigService.ADDR_MODE]
        assertEquals PersonalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig[PersonalInformationConfigService.EMER_MODE]
        assertEquals PersonalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig[PersonalInformationConfigService.PERS_DETAILS_MODE]
        assertEquals PersonalInformationConfigService.SECTION_UPDATEABLE, personalInfoConfig[PersonalInformationConfigService.ETHN_RACE_MODE]
        assertNull        personalInfoConfig[PersonalInformationConfigService.OVERVIEW_PHONE]
        assertNull        personalInfoConfig[PersonalInformationConfigService.OVERVIEW_ADDR]
    }
}
