/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 *
 */
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
    void testHasAccessToWebTailorMenu() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        assertTrue personalInformationConfigService.hasAccessToWebTailorMenu('bwgkogad.P_SelectAtypView', ['EMPLOYEE', 'STUDENT'], pidm)
    }

    @Test
    void testHasAccessToWebTailorMenuNoAccess() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        //only faculty have access
        assertFalse personalInformationConfigService.hasAccessToWebTailorMenu('bwlkoids.P_AdvEnterID', ['EMPLOYEE'], pidm)
    }

    @Test
    void testHasAccessToWebTailorMenuError() {
        def pidm = '\'cats\''

        //pidm should be a number
        assertNull personalInformationConfigService.hasAccessToWebTailorMenu('bwgkogad.P_SelectAtypView', ['EMPLOYEE'], pidm)
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
    void testGetPersonalInfoSectionConfig() {
        def pidm = PersonUtility.getPerson("GDP000005").pidm

        def piConfig = personalInformationConfigService.getPersonalInfoSectionConfig(['EMPLOYEE', 'STUDENT'], pidm)

        assertTrue piConfig.address.isVisible
        assertTrue piConfig.address.isUpdateable
    }
}
