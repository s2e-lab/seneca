package testcases;

import com.ivata.groupware.business.mail.session.MailAuthenticator;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_MailAuthenticator_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_MailAuthenticator_Simple tc = new TC_MailAuthenticator_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        MailAuthenticator object = new MailAuthenticator( "test", "test2" );
        return object;
    }

}
