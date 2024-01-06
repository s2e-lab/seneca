package testcases;

import org.exolab.castor.mapping.AccessMode;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_AccessMode_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AccessMode_Simple tc = new TC_AccessMode_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        AccessMode object = AccessMode.Shared;

        return object;
    }

}
