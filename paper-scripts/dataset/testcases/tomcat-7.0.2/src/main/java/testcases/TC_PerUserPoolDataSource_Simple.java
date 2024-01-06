package testcases;

import org.apache.tomcat.dbcp.dbcp.datasources.PerUserPoolDataSource;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_PerUserPoolDataSource_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_PerUserPoolDataSource_Simple tc = new TC_PerUserPoolDataSource_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        PerUserPoolDataSource object = new PerUserPoolDataSource();

        return object;
    }

}
