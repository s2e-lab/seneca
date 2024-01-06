package testcases;

import org.apache.commons.collections.MultiHashMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_MultiHashMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_MultiHashMap_Simple tc = new TC_MultiHashMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        MultiHashMap object = new MultiHashMap();
        object.put( "test", "test1" );
        return object;
    }

}
