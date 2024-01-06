package testcases;

import org.exolab.castor.mapping.xml.types.FieldMappingCollectionType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_FieldMappingCollectionType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FieldMappingCollectionType_Simple tc = new TC_FieldMappingCollectionType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        FieldMappingCollectionType object = FieldMappingCollectionType.ARRAY;

        return object;
    }

}
