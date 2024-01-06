package testcases;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_LoggingEvent_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_LoggingEvent_Simple tc = new TC_LoggingEvent_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        LoggingEvent object = new LoggingEvent( "test",  Logger.getLogger("test"), Level.toLevel(1), Level.toLevel(1), new Throwable() );
        object.toString();
        return object;
    }

}
