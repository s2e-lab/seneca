package testcases;

import org.apache.xalan.templates.Stylesheet;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_Stylesheet_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_Stylesheet_Simple tc = new TC_Stylesheet_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Stylesheet object = new Stylesheet(null);
        object.setHref( "test" );
        object.setId( "test2" );
        return object;
    }

}
