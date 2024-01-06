package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
*
 * This is a test case for a complex object serialization as a List
 */

public class TC_RecipientType_List extends CompositeListTestCase {

    public static void main(String[] args) throws Exception {
        TC_RecipientType_List tc = new
                TC_RecipientType_List(
                        new TC_RecipientType_Simple()
        );
        tc.runTest();
    }

    public TC_RecipientType_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
