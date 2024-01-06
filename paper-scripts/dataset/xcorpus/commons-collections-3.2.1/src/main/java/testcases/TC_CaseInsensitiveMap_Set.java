package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
*
 * This is a test case for a complex object serialization as a Set
 */

public class TC_CaseInsensitiveMap_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_CaseInsensitiveMap_Set tc = new
                TC_CaseInsensitiveMap_Set(
                        new TC_CaseInsensitiveMap_Simple()
        );
        tc.runTest();
    }

    public TC_CaseInsensitiveMap_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
