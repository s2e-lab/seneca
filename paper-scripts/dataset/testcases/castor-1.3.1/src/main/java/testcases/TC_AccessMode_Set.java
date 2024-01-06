package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_AccessMode_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_AccessMode_Set tc = new
                TC_AccessMode_Set(
                new TC_AccessMode_Simple()
        );
        tc.runTest();
    }

    public TC_AccessMode_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
