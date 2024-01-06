package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */


public class TC_ReferenceMap_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_ReferenceMap_Set tc = new
                TC_ReferenceMap_Set(
                        new TC_ReferenceMap_Simple()
        );
        tc.runTest();
    }

    public TC_ReferenceMap_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
