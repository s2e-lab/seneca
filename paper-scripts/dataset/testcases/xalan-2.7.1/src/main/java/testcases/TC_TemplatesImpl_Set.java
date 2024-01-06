package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;
/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_TemplatesImpl_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_TemplatesImpl_Set tc = new
                TC_TemplatesImpl_Set(
                        new TC_TemplatesImpl_Simple()
        );
        tc.runTest();
    }

    public TC_TemplatesImpl_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
