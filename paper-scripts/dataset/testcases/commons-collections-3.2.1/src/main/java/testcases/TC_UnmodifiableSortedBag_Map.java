package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_UnmodifiableSortedBag_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableSortedBag_Map tc = new
                TC_UnmodifiableSortedBag_Map(
                        new TC_UnmodifiableSortedBag_Simple()
        );
        tc.runTest();
    }

    public TC_UnmodifiableSortedBag_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
