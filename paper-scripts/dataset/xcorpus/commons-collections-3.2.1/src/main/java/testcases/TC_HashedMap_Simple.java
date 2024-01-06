package testcases;

import org.apache.commons.collections.map.HashedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_HashedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_HashedMap_Simple tc = new TC_HashedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        HashedMap object = new HashedMap();
        object.put( "test", "test1" );
        return object;
    }

}
