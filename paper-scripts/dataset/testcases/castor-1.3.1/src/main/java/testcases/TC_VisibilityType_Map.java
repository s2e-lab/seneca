package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_VisibilityType_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_VisibilityType_Map tc = new
                TC_VisibilityType_Map(
                new TC_VisibilityType_Simple()
        );
        tc.runTest();
    }

    public TC_VisibilityType_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
