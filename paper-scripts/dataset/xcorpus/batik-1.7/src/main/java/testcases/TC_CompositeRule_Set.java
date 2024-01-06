package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_CompositeRule_Set extends CompositeSetTestCase {

    public TC_CompositeRule_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_CompositeRule_Set tc = new
                TC_CompositeRule_Set(
                new TC_CompositeRule_Simple()
        );
        tc.runTest();
    }

}
