package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_UnmodifiableBag_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableBag_Map tc = new
                TC_UnmodifiableBag_Map(
                        new TC_UnmodifiableBag_Simple()
        );
        tc.runTest();
    }

    public TC_UnmodifiableBag_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
