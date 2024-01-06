package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_PadMode_Map extends CompositeMapTestCase {

    public TC_PadMode_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_PadMode_Map tc = new
                TC_PadMode_Map(
                new TC_PadMode_Simple()
        );
        tc.runTest();
    }

}
