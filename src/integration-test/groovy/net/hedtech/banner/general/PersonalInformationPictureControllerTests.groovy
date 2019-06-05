/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
********************************************************************************/

package net.hedtech.banner.general

import org.junit.After
import org.junit.Before
import org.junit.Test

import net.hedtech.banner.testing.BaseIntegrationTestCase

class PersonalInformationPictureControllerTests extends BaseIntegrationTestCase {

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new PersonalInformationPictureController()
        super.setUp()
    }

    /**
     * The tear down method will run after all test case method execution.
     */
    @After
    public void tearDown() {
        super.tearDown()
        super.logout()
    }


    @Test
    void testHasAccess() {
        loginSSB 'GDP000005', '111111'

        def params = [ bannerId: "GDP000005"]

        controller.params.putAll(params)
        assertTrue controller.hasAccess()
    }

    @Test
    void testHasNoAccess() {
        loginSSB 'GDP000005', '111111'

        def params = [ bannerId: "GDP000001"]

        controller.params.putAll(params)
        assertFalse controller.hasAccess()
    }

    @Test
    void testHasAccessNoId() {
        loginSSB 'GDP000005', '111111'

        def params = [ bannerId: ""]

        controller.params.putAll(params)
        assertFalse controller.hasAccess()
    }
}