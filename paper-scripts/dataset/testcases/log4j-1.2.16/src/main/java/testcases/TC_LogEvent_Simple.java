package testcases;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.pattern.LogEvent;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_LogEvent_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_LogEvent_Simple tc = new TC_LogEvent_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        LogEvent object = new LogEvent( "test",  Logger.getLogger("test"), Level.toLevel(1), Level.toLevel(1), new Throwable() );
        object.toString();
        return object;
    }

}
