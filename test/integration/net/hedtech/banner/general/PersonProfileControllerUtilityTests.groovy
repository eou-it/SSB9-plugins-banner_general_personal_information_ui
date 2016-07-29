package net.hedtech.banner.general

import java.sql.SQLException

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PersonProfileControllerUtilityTests extends BaseIntegrationTestCase {

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
        loginSSB 'GDP000005', '111111'

        def pidm = PersonProfileControllerUtility.getPrincipalPidm()
        assertNotNull pidm
    }

    @Test
    void testGetPrincipalPidmNotLoggedIn(){
        def pidm = PersonProfileControllerUtility.getPrincipalPidm()
        assertNull pidm
    }

    @Test
    void testGetFetchListParams(){
        def parameters = [ searchString: 'Mal', offset: '2', max: '10' ]
        def map = PersonProfileControllerUtility.getFetchListParams(parameters)

        assertEquals 10, map.max
        assertEquals 20, map.offset
        assertEquals 'Mal', map.searchString
    }

    @Test
    void testReturnFailureMessage(){
        String EXCEPTION_MESSAGE = "sample exception message"
        def e = new ApplicationException('entityClassOrName', EXCEPTION_MESSAGE)
        def model = PersonProfileControllerUtility.returnFailureMessage(e)

        assertNotNull model
        assertTrue(model.failure)

        String outMessage = model.message
        assertEquals(EXCEPTION_MESSAGE, outMessage)
    }

    @Test
    void testReturnFailureMessageForSQLException(){
        def sqlEx = new SQLException("SQL exception message: ORA-123456: ")
        def e = new ApplicationException('entityClassOrName', sqlEx)
        def model = PersonProfileControllerUtility.returnFailureMessage(e)

        assertNotNull model
        assertTrue(model.failure)
        assertEquals("SQLException", e.type)

        String outMessage = model.message
        assertEquals("The following error(s) have occurred: SQL exception message: ", outMessage)
    }

}
