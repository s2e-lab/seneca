package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_BindingType_Array extends CompositeArrayTestCase {

    public static void main(String[] args) throws Exception {
        TC_BindingType_Array tc = new
                TC_BindingType_Array(
                new TC_BindingType_Simple()
        );
        tc.runTest();
    }

    public TC_BindingType_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
