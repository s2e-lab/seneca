package testcases;

import org.exolab.castor.xml.schema.annotations.jdo.types.TableAccessModeType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_TableAccessModeType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_TableAccessModeType_Simple tc = new TC_TableAccessModeType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        TableAccessModeType object = TableAccessModeType.SHARED;

        return object;
    }

}
