package testcases;

import com.ivata.groupware.business.library.Library;
import com.ivata.groupware.business.library.comment.tree.CommentTreeModel;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_CommentTreeModel_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_CommentTreeModel_Simple tc = new TC_CommentTreeModel_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        com.ivata.groupware.business.library.Library library = (Library) new com.ivata.groupware.business.library.right.LibraryRightsImpl( null );
        com.ivata.groupware.admin.security.server.AbstractSecuritySession securitySession = new com.ivata.groupware.admin.security.server.AbstractSecuritySession(com.ivata.groupware.container.PicoContainerFactory.getInstance().getGlobalContainer(), new com.ivata.groupware.admin.security.user.UserDO()) {
            @Override
            public String getPassword() {
                return super.getPassword();
            }
        };
        CommentTreeModel object = new CommentTreeModel( library, securitySession );
        return object;
    }

}
