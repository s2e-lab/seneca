package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_JGraph_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_JGraph_Set tc = new
                TC_JGraph_Set(
                new TC_JGraph_Simple()
        );
        tc.runTest();
    }

    public TC_JGraph_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
