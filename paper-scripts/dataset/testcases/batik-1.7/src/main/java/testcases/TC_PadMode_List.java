package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
 * This is a test case for a complex object serialization as a List
 */

public class TC_PadMode_List extends CompositeListTestCase {

    public TC_PadMode_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_PadMode_List tc = new
                TC_PadMode_List(
                new TC_PadMode_Simple()
        );
        tc.runTest();
    }

}
