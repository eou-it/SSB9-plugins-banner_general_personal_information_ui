/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

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
}
