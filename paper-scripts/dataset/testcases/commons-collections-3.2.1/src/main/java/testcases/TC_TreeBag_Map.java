package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_TreeBag_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_TreeBag_Map tc = new
                TC_TreeBag_Map(
                        new TC_TreeBag_Simple()
        );
        tc.runTest();
    }

    public TC_TreeBag_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
