package testcases;

import org.apache.commons.collections.bidimap.DualTreeBidiMap;
import org.apache.commons.collections.map.UnmodifiableSortedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnmodifiableSortedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableSortedMap_Simple tc = new TC_UnmodifiableSortedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        DualTreeBidiMap map = new DualTreeBidiMap();
        map.put("testKey", "testVal");
        UnmodifiableSortedMap object = (UnmodifiableSortedMap) UnmodifiableSortedMap.decorate(map);
        return object;
    }

}
