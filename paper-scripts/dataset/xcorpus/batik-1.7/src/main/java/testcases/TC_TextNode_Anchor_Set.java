package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_TextNode_Anchor_Set extends CompositeSetTestCase {

    public TC_TextNode_Anchor_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_TextNode_Anchor_Set tc = new
                TC_TextNode_Anchor_Set(
                new TC_TextNode_Anchor_Simple()
        );
        tc.runTest();
    }

}
