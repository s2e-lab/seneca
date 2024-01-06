package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_AbstractDocument_Map extends CompositeMapTestCase {

    public TC_AbstractDocument_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_AbstractDocument_Map tc = new
                TC_AbstractDocument_Map(
                new TC_AbstractDocument_Simple()
        );
        tc.runTest();
    }

}
