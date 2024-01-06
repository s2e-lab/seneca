package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_InputStreamSource_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_InputStreamSource_Map tc = new
                TC_InputStreamSource_Map(
                        new TC_InputStreamSource_Simple()
        );
        tc.runTest();
    }

    public TC_InputStreamSource_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
