package testcases;

import org.apache.commons.collections.bag.HashBag;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_HashBag_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_HashBag_Simple tc = new TC_HashBag_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        HashBag hashBag = new HashBag();
        hashBag.add( "test" );
        return hashBag;
    }

}
