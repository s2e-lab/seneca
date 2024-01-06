package testcases;

import org.exolab.castor.builder.binding.xml.types.VisibilityType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_VisibilityType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_VisibilityType_Simple tc = new TC_VisibilityType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        VisibilityType object = VisibilityType.PUBLIC;

        return object;
    }

}
