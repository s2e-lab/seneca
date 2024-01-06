package testcases;

import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import org.apache.commons.collections.buffer.UnmodifiableBuffer;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnmodifiableFifoBuffer_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableFifoBuffer_Simple tc = new TC_UnmodifiableFifoBuffer_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        BoundedFifoBuffer buffer = new BoundedFifoBuffer();
        buffer.add( "test" );
        UnmodifiableBuffer object = (UnmodifiableBuffer) UnmodifiableBuffer.decorate( buffer );
        return object;
    }

}
