package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
*
 * This is a test case for a complex object serialization as a List
 */

public class TC_CaseInsensitiveMap_List extends CompositeListTestCase {

    public static void main(String[] args) throws Exception {
        TC_CaseInsensitiveMap_List tc = new
                TC_CaseInsensitiveMap_List(
                        new TC_CaseInsensitiveMap_Simple()
        );
        tc.runTest();
    }

    public TC_CaseInsensitiveMap_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
