package testcases;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_DualHashBidiMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_DualHashBidiMap_Simple tc = new TC_DualHashBidiMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        DualHashBidiMap object = new DualHashBidiMap();
        object.put( "test", "test1" );
        return object;
    }

}
