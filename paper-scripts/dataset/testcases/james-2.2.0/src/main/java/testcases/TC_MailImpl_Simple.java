package testcases;

import org.apache.james.core.MailImpl;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_MailImpl_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_MailImpl_Simple tc = new TC_MailImpl_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        MailImpl object = new MailImpl();
        return object;
    }

}
