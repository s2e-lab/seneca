package testcases;

import org.apache.commons.collections.map.LinkedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_LinkedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_LinkedMap_Simple tc = new TC_LinkedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        LinkedMap object = new LinkedMap();
        object.put( "test", "test1" );
        return object;
    }

}
