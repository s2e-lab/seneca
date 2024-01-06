package testcases;

import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_BoundedFifoBuffer_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_BoundedFifoBuffer_Simple tc = new TC_BoundedFifoBuffer_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        BoundedFifoBuffer object = new BoundedFifoBuffer();
        object.add( "test" );
        return object;
    }

}
