package testcases;

import org.apache.xalan.client.XSLTProcessorApplet;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_XSLTProcessorApplet_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_XSLTProcessorApplet_Simple tc = new TC_XSLTProcessorApplet_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        XSLTProcessorApplet object = new XSLTProcessorApplet();
        return object;
    }

}
