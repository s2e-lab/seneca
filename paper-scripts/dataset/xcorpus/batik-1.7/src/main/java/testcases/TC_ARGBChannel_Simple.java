package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import org.apache.batik.ext.awt.image.ARGBChannel;


/**
 * This is a test case for a simple object serialization
 */

public class TC_ARGBChannel_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_ARGBChannel_Simple tc = new TC_ARGBChannel_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        ARGBChannel object = ARGBChannel.A;

        return object;
    }

}
