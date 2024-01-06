package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import org.apache.batik.ext.awt.image.PadMode;


/**
 * This is a test case for a simple object serialization
 */

public class TC_PadMode_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_PadMode_Simple tc = new TC_PadMode_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        PadMode object = PadMode.ZERO_PAD;

        return object;
    }

}
