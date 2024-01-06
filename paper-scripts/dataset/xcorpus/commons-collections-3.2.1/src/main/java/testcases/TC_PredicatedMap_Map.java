package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_PredicatedMap_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_PredicatedMap_Map tc = new
                TC_PredicatedMap_Map(
                        new TC_PredicatedMap_Simple()
        );
        tc.runTest();
    }

    public TC_PredicatedMap_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
