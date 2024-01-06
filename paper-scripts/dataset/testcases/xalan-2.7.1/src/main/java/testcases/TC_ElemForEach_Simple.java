package testcases;

import org.apache.xalan.templates.ElemForEach;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_ElemForEach_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_ElemForEach_Simple tc = new TC_ElemForEach_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        ElemForEach object = new ElemForEach();
        return object;
    }

}
