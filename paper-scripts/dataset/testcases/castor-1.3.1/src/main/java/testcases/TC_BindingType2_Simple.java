package testcases;

import org.castor.mapping.BindingType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_BindingType2_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_BindingType2_Simple tc = new TC_BindingType2_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        BindingType object = BindingType.XML;

        return object;
    }

}
