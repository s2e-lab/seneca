package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_UnicastDelegate_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnicastDelegate_Set tc = new
                TC_UnicastDelegate_Set(
                new TC_UnicastDelegate_Simple()
        );
        tc.runTest();
    }

    public TC_UnicastDelegate_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
