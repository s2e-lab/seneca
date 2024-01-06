package testcases;

import com.ivata.groupware.business.addressbook.person.group.right.UserRightFilter;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_UserRightFilter_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UserRightFilter_Simple tc = new TC_UserRightFilter_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        UserRightFilter object = new UserRightFilter();
        return object;
    }

}
