package testcases;

import org.exolab.castor.builder.binding.xml.types.FieldTypeVisibilityType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_FieldTypeVisibilityType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FieldTypeVisibilityType_Simple tc = new TC_FieldTypeVisibilityType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        FieldTypeVisibilityType object = FieldTypeVisibilityType.PUBLIC;

        return object;
    }

}
