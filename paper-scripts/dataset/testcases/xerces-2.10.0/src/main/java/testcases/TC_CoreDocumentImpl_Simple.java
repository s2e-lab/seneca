package testcases;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.w3c.dom.ElementTraversal;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_CoreDocumentImpl_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_CoreDocumentImpl_Simple tc = new TC_CoreDocumentImpl_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        CoreDocumentImpl object = new CoreDocumentImpl();
        object.setDocumentURI("test");
        object.setInputEncoding("test2");
        object.setTextContent("test3");
        object.setXmlEncoding("test4");
        object.setNodeValue("test5");
        return object;
    }

}
