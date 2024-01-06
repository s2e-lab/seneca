package testcases;

import com.ivata.groupware.admin.security.server.AbstractSecuritySession;
import com.ivata.groupware.admin.security.user.UserDO;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_AbstractSecuritySession_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractSecuritySession_Simple tc = new TC_AbstractSecuritySession_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        AbstractSecuritySession object = new AbstractSecuritySession(com.ivata.groupware.container.PicoContainerFactory.getInstance().getGlobalContainer(), new UserDO()) {
            @Override
            public String getPassword() {
                return super.getPassword();
            }
        };
        return object;
    }

}
