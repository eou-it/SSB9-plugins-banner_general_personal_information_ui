/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
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
}
