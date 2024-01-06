package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_UnicastDelegate_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnicastDelegate_Map tc = new
                TC_UnicastDelegate_Map(
                new TC_UnicastDelegate_Simple()
        );
        tc.runTest();
    }

    public TC_UnicastDelegate_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
