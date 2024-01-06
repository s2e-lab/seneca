package testcases;

import com.ivata.groupware.business.addressbook.AddressBookBean;
import com.ivata.groupware.business.addressbook.person.group.tree.PersonTreeModel;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_PersonTreeModel_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_PersonTreeModel_Simple tc = new TC_PersonTreeModel_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        com.ivata.groupware.admin.security.server.AbstractSecuritySession securitySession = new com.ivata.groupware.admin.security.server.AbstractSecuritySession(com.ivata.groupware.container.PicoContainerFactory.getInstance().getGlobalContainer(), new com.ivata.groupware.admin.security.user.UserDO()) {
            @Override
            public String getPassword() {
                return super.getPassword();
            }
        };

        PersonTreeModel object = new com.ivata.groupware.business.addressbook.person.group.tree.PersonTreeModel( new AddressBookBean(), securitySession);
        return object;
    }

}
