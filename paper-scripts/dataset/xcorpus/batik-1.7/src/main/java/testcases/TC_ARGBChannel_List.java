package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
 * This is a test case for a complex object serialization as a List
 */

public class TC_ARGBChannel_List extends CompositeListTestCase {

    public TC_ARGBChannel_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_ARGBChannel_List tc = new
                TC_ARGBChannel_List(
                new TC_ARGBChannel_Simple()
        );
        tc.runTest();
    }

}
