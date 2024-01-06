package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_TextNode_Anchor_Array extends CompositeArrayTestCase {

    public TC_TextNode_Anchor_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_TextNode_Anchor_Array tc = new
                TC_TextNode_Anchor_Array(
                new TC_TextNode_Anchor_Simple()
        );
        tc.runTest();
    }

}
