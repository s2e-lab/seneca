package testcases;

import org.apache.commons.collections.map.UnmodifiableMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.HashMap;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnmodifiableMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableMap_Simple tc = new TC_UnmodifiableMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        HashMap map = new HashMap();
        map.put("testKey", "testVal");
        UnmodifiableMap object = (UnmodifiableMap) UnmodifiableMap.decorate(map);
        return object;
    }

}
