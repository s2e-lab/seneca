package testcases;

import org.java.plugin.registry.Version;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_Version_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_Version_Simple tc = new TC_Version_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Version object = Version.parse( "test" );
        return object;
    }

}
