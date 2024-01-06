package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_MultiHashMap_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_MultiHashMap_Map tc = new
                TC_MultiHashMap_Map(
                        new TC_MultiHashMap_Simple()
        );
        tc.runTest();
    }

    public TC_MultiHashMap_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
