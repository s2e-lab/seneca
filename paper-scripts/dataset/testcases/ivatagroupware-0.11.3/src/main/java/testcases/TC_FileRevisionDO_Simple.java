package testcases;

import com.ivata.groupware.business.drive.file.FileRevisionDO;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_FileRevisionDO_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_FileRevisionDO_Simple tc = new TC_FileRevisionDO_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        FileRevisionDO object = new FileRevisionDO();
        return object;
    }

}
