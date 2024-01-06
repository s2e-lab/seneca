package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;

/**
 * This is a test case for a simple object serialization
 */

public class TC_NicelyResynchronizingAjaxController_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_NicelyResynchronizingAjaxController_Simple tc = new TC_NicelyResynchronizingAjaxController_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        NicelyResynchronizingAjaxController object = new NicelyResynchronizingAjaxController();

        return object;
    }

}
