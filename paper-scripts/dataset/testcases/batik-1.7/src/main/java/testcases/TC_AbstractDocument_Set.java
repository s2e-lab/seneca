package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_AbstractDocument_Set extends CompositeSetTestCase {

    public TC_AbstractDocument_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_AbstractDocument_Set tc = new
                TC_AbstractDocument_Set(
                new TC_AbstractDocument_Simple()
        );
        tc.runTest();
    }

}
