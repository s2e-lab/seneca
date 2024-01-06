package testcases;

import org.exolab.jms.net.orb.UnicastDelegate;
import org.apache.commons.logging.LogFactory;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.rmi.server.ObjID;


/**
 * This is a test case for a simple object serialization
 */

public class TC_UnicastDelegate_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnicastDelegate_Simple tc = new TC_UnicastDelegate_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        UnicastDelegate object = new UnicastDelegate(new ObjID(), "https://example.org");

        return object;
    }

}
