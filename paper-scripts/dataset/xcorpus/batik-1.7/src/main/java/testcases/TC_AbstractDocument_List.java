package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
 * This is a test case for a complex object serialization as a List
 */

public class TC_AbstractDocument_List extends CompositeListTestCase {

    public TC_AbstractDocument_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_AbstractDocument_List tc = new
                TC_AbstractDocument_List(
                new TC_AbstractDocument_Simple()
        );
        tc.runTest();
    }

}
