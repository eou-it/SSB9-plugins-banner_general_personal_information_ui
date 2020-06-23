/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import grails.util.Holders
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class BasePersonConfigServiceIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        Holders.config.'personalInfo.prefEmailUpdatability' = 1
        formContext = ['GUAGMNU','SELFSERVICE']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testGetParamFromSessionWithNoPreexistingConfig() {
        def configService = getMockPersonalInfoConfigService()
        def val = configService.getParamFromSession('personalInfo.prefEmailUpdatability', 'dummy_default_value')

        assertEquals PersonalInformationConfigService.SECTION_READONLY, val
    }

    @Test
    void testGetParamFromSessionWithPreexistingSessionConfigButNoPersonalInfoConfig() {
        def personConfigInSession = [dummy: [dummy: 'Y']]
        PersonUtility.setPersonConfigInSession(personConfigInSession)

        def configService = getMockPersonalInfoConfigService()
        def val = configService.getParamFromSession('personalInfo.prefEmailUpdatability', 'dummy_default_value')

        assertEquals PersonalInformationConfigService.SECTION_READONLY, val
    }

    @Test
    void testGetParamFromSessionBadKeyAndDefaultValue() {
        def configService = getMockPersonalInfoConfigService()
        def val = configService.getParamFromSession('I_DONT_EXIST', 'dummy_default_value')

        assertEquals "dummy_default_value", val
    }

    @Test
    void testGetParamFromSessionExcludedPropertyPERS_INFO_OVERVIEW_ADDRESS() {
        def configService = getMockPersonalInfoConfigService()
        def val = configService.getParamFromSession(PersonalInformationConfigService.OVERVIEW_ADDR, null)

        assertNull val
    }

    @Test
    void testGetParamFromSessionExcludedPropertyPERS_INFO_OVERVIEW_PHONE() {
        def configService = getMockPersonalInfoConfigService()
        def val = configService.getParamFromSession(PersonalInformationConfigService.OVERVIEW_PHONE, null)

        assertNull val
    }

    private def getMockPersonalInfoConfigService(){
        def mockPersonalInfoConfigService = [
                getCacheName: {-> return PersonalInformationConfigService.PERSONAL_INFO_CONFIG_CACHE_NAME },
                getProcessCode: {-> return PersonalInformationConfigService.PERSONAL_INFO_PROCESS_CODE },
                getExcludedProperties: {-> return  [PersonalInformationConfigService.OVERVIEW_ADDR, PersonalInformationConfigService.OVERVIEW_PHONE]}
        ] as BasePersonConfigService

        mockPersonalInfoConfigService
    }
}
