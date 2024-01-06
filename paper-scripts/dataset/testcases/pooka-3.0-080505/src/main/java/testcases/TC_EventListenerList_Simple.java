package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import javax.help.event.EventListenerList;
import java.util.EventListener;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_EventListenerList_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_EventListenerList_Simple tc = new TC_EventListenerList_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        EventListenerList object = new EventListenerList();
        return object;
    }

}
