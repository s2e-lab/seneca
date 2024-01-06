package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */

public class TC_FixedSizeSortedMap_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_FixedSizeSortedMap_Set tc = new
                TC_FixedSizeSortedMap_Set(
                        new TC_FixedSizeSortedMap_Simple()
        );
        tc.runTest();
    }

    public TC_FixedSizeSortedMap_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
