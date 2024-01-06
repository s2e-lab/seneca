package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_PadMode_Array extends CompositeArrayTestCase {

    public TC_PadMode_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_PadMode_Array tc = new
                TC_PadMode_Array(
                new TC_PadMode_Simple()
        );
        tc.runTest();
    }

}
