package testcases;

import org.exolab.castor.mapping.xml.types.ClassMappingAccessType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_ClassMappingAccessType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_ClassMappingAccessType_Simple tc = new TC_ClassMappingAccessType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        ClassMappingAccessType object = ClassMappingAccessType.SHARED;

        return object;
    }

}
