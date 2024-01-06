package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_ARGBChannel_Set extends CompositeSetTestCase {

    public TC_ARGBChannel_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_ARGBChannel_Set tc = new
                TC_ARGBChannel_Set(
                new TC_ARGBChannel_Simple()
        );
        tc.runTest();
    }

}
