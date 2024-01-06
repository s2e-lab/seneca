package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_TextNode_Anchor_Map extends CompositeMapTestCase {

    public TC_TextNode_Anchor_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_TextNode_Anchor_Map tc = new
                TC_TextNode_Anchor_Map(
                new TC_TextNode_Anchor_Simple()
        );
        tc.runTest();
    }

}
