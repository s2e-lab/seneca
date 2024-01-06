package testcases;

import org.exolab.castor.mapping.xml.types.BindXmlNodeType;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_BindXmlNodeType_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_BindXmlNodeType_Simple tc = new TC_BindXmlNodeType_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        BindXmlNodeType object = BindXmlNodeType.ELEMENT;

        return object;
    }

}
