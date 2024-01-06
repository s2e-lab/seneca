package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import javax.mail.internet.MimeMessage;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_RecipientType2_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_RecipientType2_Simple tc = new TC_RecipientType2_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        MimeMessage.RecipientType object = MimeMessage.RecipientType.NEWSGROUPS;
        object.toString();
        return object;
    }

}
