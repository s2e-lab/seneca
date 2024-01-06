package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;
/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_PersonTreeModel_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_PersonTreeModel_Set tc = new
                TC_PersonTreeModel_Set(
                        new TC_PersonTreeModel_Simple()
        );
        tc.runTest();
    }

    public TC_PersonTreeModel_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
