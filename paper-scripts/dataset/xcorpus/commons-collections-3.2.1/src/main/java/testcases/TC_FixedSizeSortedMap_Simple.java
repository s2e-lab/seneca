package testcases;

import org.apache.commons.collections.map.FixedSizeMap;
import org.apache.commons.collections.map.FixedSizeSortedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.TreeMap;

/**
*
 * This is a test case for a simple object serialization
 */

public class TC_FixedSizeSortedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FixedSizeSortedMap_Simple tc = new TC_FixedSizeSortedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        TreeMap map = new TreeMap<String, String>();
        map.put("testKey", "testVal");
        FixedSizeSortedMap object = (FixedSizeSortedMap) FixedSizeSortedMap.decorate(map);
        return object;
    }

}
