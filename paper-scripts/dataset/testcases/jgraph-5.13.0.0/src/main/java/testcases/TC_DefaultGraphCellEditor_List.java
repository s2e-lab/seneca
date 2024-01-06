package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

/**
 * This is a test case for a complex object serialization as a List
 */

public class TC_DefaultGraphCellEditor_List extends CompositeListTestCase {

    public static void main(String[] args) throws Exception {
        TC_DefaultGraphCellEditor_List tc = new
                TC_DefaultGraphCellEditor_List(
                new TC_DefaultGraphCellEditor_Simple()
        );
        tc.runTest();
    }

    public TC_DefaultGraphCellEditor_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
