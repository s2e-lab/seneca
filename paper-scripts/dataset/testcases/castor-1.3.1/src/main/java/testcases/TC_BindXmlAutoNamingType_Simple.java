package testcases;

import org.exolab.castor.mapping.xml.types.BindXmlAutoNamingType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_BindXmlAutoNamingType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_BindXmlAutoNamingType_Simple tc = new TC_BindXmlAutoNamingType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        BindXmlAutoNamingType object = BindXmlAutoNamingType.DERIVEBYCLASS;

        return object;
    }

}
