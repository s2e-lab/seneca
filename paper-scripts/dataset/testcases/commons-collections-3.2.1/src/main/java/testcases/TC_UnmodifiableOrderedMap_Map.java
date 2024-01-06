package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_UnmodifiableOrderedMap_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableOrderedMap_Map tc = new
                TC_UnmodifiableOrderedMap_Map(
                        new TC_UnmodifiableOrderedMap_Simple()
        );
        tc.runTest();
    }

    public TC_UnmodifiableOrderedMap_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
