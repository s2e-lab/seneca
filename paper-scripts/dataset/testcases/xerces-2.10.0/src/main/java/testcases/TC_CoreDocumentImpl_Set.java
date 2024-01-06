package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;
/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_CoreDocumentImpl_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_CoreDocumentImpl_Set tc = new
                TC_CoreDocumentImpl_Set(
                        new TC_CoreDocumentImpl_Simple()
        );
        tc.runTest();
    }

    public TC_CoreDocumentImpl_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
