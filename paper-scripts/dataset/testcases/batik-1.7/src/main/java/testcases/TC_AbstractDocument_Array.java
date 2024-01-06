package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_AbstractDocument_Array extends CompositeArrayTestCase {

    public TC_AbstractDocument_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_AbstractDocument_Array tc = new
                TC_AbstractDocument_Array(
                new TC_AbstractDocument_Simple()
        );
        tc.runTest();
    }

}
