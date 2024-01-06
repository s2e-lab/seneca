package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_UnmodifiableSortedMap_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableSortedMap_Map tc = new
                TC_UnmodifiableSortedMap_Map(
                        new TC_UnmodifiableSortedMap_Simple()
        );
        tc.runTest();
    }

    public TC_UnmodifiableSortedMap_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
