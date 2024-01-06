package testcases;

import org.exolab.castor.mapping.xml.types.SqlDirtyType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_SqlDirtyType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_SqlDirtyType_Simple tc = new TC_SqlDirtyType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        SqlDirtyType object = SqlDirtyType.CHECK;

        return object;
    }

}
