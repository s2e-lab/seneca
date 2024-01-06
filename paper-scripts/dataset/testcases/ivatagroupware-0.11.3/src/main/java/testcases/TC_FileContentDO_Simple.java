package testcases;

import com.ivata.groupware.business.drive.file.FileContentDO;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_FileContentDO_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FileContentDO_Simple tc = new TC_FileContentDO_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        FileContentDO object = new FileContentDO( new SerializedByteArray(), "test" );
        return object;
    }

}
