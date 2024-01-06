package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */

public class TC_Level_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_Level_Set tc = new
                TC_Level_Set(
                        new TC_Level_Simple()
        );
        tc.runTest();
    }

    public TC_Level_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
