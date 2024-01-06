package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import org.apache.batik.ext.awt.image.CompositeRule;


/**
 * This is a test case for a simple object serialization
 */

public class TC_CompositeRule_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_CompositeRule_Simple tc = new TC_CompositeRule_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        CompositeRule object = CompositeRule.OUT;

        return object;
    }

}
