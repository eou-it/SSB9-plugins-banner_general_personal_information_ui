package net.hedtech.banner.general

import java.sql.SQLException

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class ControllerUtilityTests extends BaseIntegrationTestCase {

    /**
     * The setup method will run before all test case method executions start.
     */
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new PersonProfileDetailsController()
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
    void testGetPrincipalPidmLoggedIn(){
        loginSSB 'MYE000001', '111111'

        def pidm = ControllerUtility.getPrincipalPidm()
        assertNotNull pidm
        assertEquals(36732, pidm)
    }

    @Test
    void testGetPrincipalPidmNotLoggedIn(){
        def pidm = ControllerUtility.getPrincipalPidm()
        assertNull pidm
    }

    @Test
    void testReturnFailureMessage(){
        String EXCEPTION_MESSAGE = "sample exception message"
        def e = new ApplicationException('entityClassOrName', EXCEPTION_MESSAGE)
        def model = ControllerUtility.returnFailureMessage(e)

        assertNotNull model
        assertTrue(model.failure)

        String outMessage = model.message
        assertEquals(EXCEPTION_MESSAGE, outMessage)
    }

    @Test
    void testReturnFailureMessageForSQLException(){
        def sqlEx = new SQLException("SQL exception message: ORA-123456: ")
        def e = new ApplicationException('entityClassOrName', sqlEx)
        def model = ControllerUtility.returnFailureMessage(e)

        assertNotNull model
        assertTrue(model.failure)
        assertEquals("SQLException", e.type)

        String outMessage = model.message
        assertEquals("The following error(s) have occurred: SQL exception message: ", outMessage)
    }

}
