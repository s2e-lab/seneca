package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_AccessMode_Array extends CompositeArrayTestCase {

    public static void main(String[] args) throws Exception {
        TC_AccessMode_Array tc = new
                TC_AccessMode_Array(
                new TC_AccessMode_Simple()
        );
        tc.runTest();
    }

    public TC_AccessMode_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
