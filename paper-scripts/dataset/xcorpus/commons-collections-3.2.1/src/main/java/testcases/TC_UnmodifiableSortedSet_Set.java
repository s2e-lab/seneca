package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */

public class TC_UnmodifiableSortedSet_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableSortedSet_Set tc = new
                TC_UnmodifiableSortedSet_Set(
                        new TC_UnmodifiableSortedSet_Simple()
        );
        tc.runTest();
    }

    public TC_UnmodifiableSortedSet_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
