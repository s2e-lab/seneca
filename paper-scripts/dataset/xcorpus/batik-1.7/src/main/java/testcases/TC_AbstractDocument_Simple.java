package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.GenericDocument;
import org.apache.batik.dom.GenericDocumentType;


/**
 * This is a test case for a simple object serialization
 */

public class TC_AbstractDocument_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractDocument_Simple tc = new TC_AbstractDocument_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        AbstractDocument object = new GenericDocument(new GenericDocumentType("arg1", "arg2", "arg3"), new GenericDOMImplementation());
        return object;
    }

}
