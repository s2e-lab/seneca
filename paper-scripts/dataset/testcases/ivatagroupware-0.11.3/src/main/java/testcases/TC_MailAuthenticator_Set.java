package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;
/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_MailAuthenticator_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_MailAuthenticator_Set tc = new
                TC_MailAuthenticator_Set(
                        new TC_MailAuthenticator_Simple()
        );
        tc.runTest();
    }

    public TC_MailAuthenticator_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
