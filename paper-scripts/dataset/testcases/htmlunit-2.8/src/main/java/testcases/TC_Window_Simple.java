package testcases;

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_Window_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_Window_Simple tc = new TC_Window_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        Window object = new Window();

        return object;
    }

}
