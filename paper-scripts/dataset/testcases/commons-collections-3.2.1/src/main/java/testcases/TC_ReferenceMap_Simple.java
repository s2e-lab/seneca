package testcases;

import org.apache.commons.collections.map.ReferenceMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_ReferenceMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_ReferenceMap_Simple tc = new TC_ReferenceMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        ReferenceMap referenceMap = new ReferenceMap();
        referenceMap.put( "testKey", "testVal" );
        return referenceMap;
    }

}
