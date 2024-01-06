package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */

public class TC_LogEvent_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_LogEvent_Set tc = new
                TC_LogEvent_Set(
                        new TC_LogEvent_Simple()
        );
        tc.runTest();
    }

    public TC_LogEvent_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
