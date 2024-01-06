package testcases;

import org.apache.commons.collections.map.ListOrderedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_ListOrderedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_ListOrderedMap_Simple tc = new TC_ListOrderedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        ListOrderedMap object = new ListOrderedMap();
        object.put( "test", "test1" );
        return object;
    }

}
