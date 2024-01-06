package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_CursorableLinkedList_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_CursorableLinkedList_Set tc = new
                TC_CursorableLinkedList_Set(
                        new TC_CursorableLinkedList_Simple()
        );
        tc.runTest();
    }

    public TC_CursorableLinkedList_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
