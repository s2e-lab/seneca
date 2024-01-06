package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_ReferenceMap_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_ReferenceMap_Map tc = new
                TC_ReferenceMap_Map(
                        new TC_ReferenceMap_Simple()
        );
        tc.runTest();
    }

    public TC_ReferenceMap_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
