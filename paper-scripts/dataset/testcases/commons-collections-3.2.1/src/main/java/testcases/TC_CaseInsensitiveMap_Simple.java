package testcases;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_CaseInsensitiveMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_CaseInsensitiveMap_Simple tc = new TC_CaseInsensitiveMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        CaseInsensitiveMap object = new CaseInsensitiveMap();
        object.put( "test", "test1" );
        return object;
    }

}
