package testcases;

import org.apache.xpath.axes.UnionPathIterator;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_UnionPathIterator_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnionPathIterator_Simple tc = new TC_UnionPathIterator_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        UnionPathIterator object = new UnionPathIterator();
        return object;
    }

}
