package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
*
 * This is a test case for a complex object serialization as a Map
 */

public class TC_CaseInsensitiveMap_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_CaseInsensitiveMap_Map tc = new
                TC_CaseInsensitiveMap_Map(
                        new TC_CaseInsensitiveMap_Simple()
        );
        tc.runTest();
    }

    public TC_CaseInsensitiveMap_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
