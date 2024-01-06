package testcases;

import org.apache.commons.collections.map.ReferenceIdentityMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_ReferencedIdentityMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_ReferencedIdentityMap_Simple tc = new TC_ReferencedIdentityMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        ReferenceIdentityMap object = new ReferenceIdentityMap();
        object.put( "test", "test1" );
        return object;
    }

}
