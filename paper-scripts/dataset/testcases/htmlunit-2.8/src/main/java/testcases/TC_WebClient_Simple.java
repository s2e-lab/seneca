package testcases;

import com.gargoylesoftware.htmlunit.WebClient;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_WebClient_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_WebClient_Simple tc = new TC_WebClient_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        WebClient object = new WebClient();

        return object;
    }

}
