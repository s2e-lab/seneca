package testcases;

import org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSource;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_SharedPoolDataSource_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_SharedPoolDataSource_Simple tc = new TC_SharedPoolDataSource_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        SharedPoolDataSource object = new SharedPoolDataSource();

        return object;
    }

}
