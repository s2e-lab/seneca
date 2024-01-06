package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_PerUserPoolDataSource_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_PerUserPoolDataSource_Set tc = new
                TC_PerUserPoolDataSource_Set(
                new TC_PerUserPoolDataSource_Simple()
        );
        tc.runTest();
    }

    public TC_PerUserPoolDataSource_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
