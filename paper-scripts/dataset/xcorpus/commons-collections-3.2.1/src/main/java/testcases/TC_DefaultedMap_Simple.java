package testcases;

import org.apache.commons.collections.map.DefaultedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_DefaultedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_DefaultedMap_Simple tc = new TC_DefaultedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        DefaultedMap object = new DefaultedMap( "test2" );
        object.put( "test", "test1" );
        return object;
    }

}
