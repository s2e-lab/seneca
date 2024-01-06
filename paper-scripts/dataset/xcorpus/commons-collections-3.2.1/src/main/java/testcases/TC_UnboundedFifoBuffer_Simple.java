package testcases;

import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnboundedFifoBuffer_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnboundedFifoBuffer_Simple tc = new TC_UnboundedFifoBuffer_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        UnboundedFifoBuffer object = new UnboundedFifoBuffer();
        object.add( "test" );
        return object;
    }

}
