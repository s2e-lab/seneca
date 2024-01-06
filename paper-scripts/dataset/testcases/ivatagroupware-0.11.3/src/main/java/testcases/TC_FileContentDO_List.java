package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;
/**
 * This is a test case for a complex object serialization as a List
 */

public class TC_FileContentDO_List extends CompositeListTestCase {

    public static void main(String[] args) throws Exception {
        TC_FileContentDO_List tc = new
                TC_FileContentDO_List(
                        new TC_FileContentDO_Simple()
        );
        tc.runTest();
    }

    public TC_FileContentDO_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
