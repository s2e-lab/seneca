package testcases;

import org.apache.xalan.xsltc.trax.TemplatesImpl;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_TemplatesImpl_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_TemplatesImpl_Simple tc = new TC_TemplatesImpl_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        TemplatesImpl object = new TemplatesImpl();
        return object;
    }

}
