package testcases;

import org.apache.commons.collections.map.Flat3Map;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_Flat3Map_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_Flat3Map_Simple tc = new TC_Flat3Map_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Flat3Map object = new Flat3Map();
        object.put( "test", "test1" );
        return object;
    }

}
