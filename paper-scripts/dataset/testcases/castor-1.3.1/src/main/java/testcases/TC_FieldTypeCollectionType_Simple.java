package testcases;

import org.exolab.castor.builder.binding.xml.types.FieldTypeCollectionType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_FieldTypeCollectionType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FieldTypeCollectionType_Simple tc = new TC_FieldTypeCollectionType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        FieldTypeCollectionType object = FieldTypeCollectionType.ARRAY;

        return object;
    }

}
