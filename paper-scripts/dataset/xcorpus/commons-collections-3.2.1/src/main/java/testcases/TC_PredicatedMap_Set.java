package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */

public class TC_PredicatedMap_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_PredicatedMap_Set tc = new
                TC_PredicatedMap_Set(
                        new TC_PredicatedMap_Simple()
        );
        tc.runTest();
    }

    public TC_PredicatedMap_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
