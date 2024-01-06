package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_CompositeRule_Array extends CompositeArrayTestCase {

    public TC_CompositeRule_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_CompositeRule_Array tc = new
                TC_CompositeRule_Array(
                new TC_CompositeRule_Simple()
        );
        tc.runTest();
    }

}
