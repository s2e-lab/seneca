package testcases;

import org.apache.commons.collections.map.FixedSizeMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.HashMap;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_FixedSizeMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FixedSizeMap_Simple tc = new TC_FixedSizeMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        HashMap hashMap = new HashMap<String, String>();
        hashMap.put("testKey", "testVal");
        FixedSizeMap object = (FixedSizeMap) FixedSizeMap.decorate(hashMap);
        return object;
    }

}
