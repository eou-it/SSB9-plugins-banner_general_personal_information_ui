/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


class PersonalInformationServiceTests extends BaseIntegrationTestCase {

    def personalInformationService

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
    void testGetCurrentName() {
        loginSSB 'HOSH00018', '111111'
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()

        def val = personalInformationService.getCurrentName(pidm)

        assertNotNull(val)
        assertEquals '2Curr2ProgSameStudyPath', val.firstName
        assertNull val.middleName
        assertEquals 'EBRUSER1', val.lastName
    }
}
