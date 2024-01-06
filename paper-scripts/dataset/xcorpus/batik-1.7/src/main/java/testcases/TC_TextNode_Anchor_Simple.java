package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import org.apache.batik.gvt.TextNode;


/**
 * This is a test case for a simple object serialization
 */

public class TC_TextNode_Anchor_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_TextNode_Anchor_Simple tc = new TC_TextNode_Anchor_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        TextNode.Anchor object = TextNode.Anchor.START;

        return object;
    }

}
