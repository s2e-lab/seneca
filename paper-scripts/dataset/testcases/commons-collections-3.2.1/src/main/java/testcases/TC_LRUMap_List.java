package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
*
 * This is a test case for a complex object serialization as a List
 */

public class TC_LRUMap_List extends CompositeListTestCase {

    public static void main(String[] args) throws Exception {
        TC_LRUMap_List tc = new
                TC_LRUMap_List(
                        new TC_LRUMap_Simple()
        );
        tc.runTest();
    }

    public TC_LRUMap_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
