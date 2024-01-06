package testcases;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.map.UnmodifiableOrderedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnmodifiableOrderedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableOrderedMap_Simple tc = new TC_UnmodifiableOrderedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        UnmodifiableOrderedMap object = (UnmodifiableOrderedMap) UnmodifiableOrderedMap.decorate(new LinkedMap());
        return object;
    }

}
