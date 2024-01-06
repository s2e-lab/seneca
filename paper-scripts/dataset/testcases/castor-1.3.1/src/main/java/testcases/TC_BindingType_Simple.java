package testcases;

import org.exolab.castor.builder.binding.xml.types.BindingType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_BindingType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_BindingType_Simple tc = new TC_BindingType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        BindingType object = BindingType.ELEMENT;

        return object;
    }

}
