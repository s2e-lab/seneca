package testcases;

import org.apache.commons.collections.map.IdentityMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_IdentityMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_IdentityMap_Simple tc = new TC_IdentityMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        IdentityMap object = new IdentityMap();
        object.put( "test", "test1" );
        return object;
    }

}
