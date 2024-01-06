package testcases;

import com.gargoylesoftware.htmlunit.javascript.host.History;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_History_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_History_Simple tc = new TC_History_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
	// FrameWindow implements WebWindow
        History object = new History();

        return object;
    }

}
