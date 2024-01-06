package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import javax.mail.Message;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_RecipientType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_RecipientType_Simple tc = new TC_RecipientType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Message.RecipientType object = Message.RecipientType.TO;
        object.toString();
        return object;
    }

}
