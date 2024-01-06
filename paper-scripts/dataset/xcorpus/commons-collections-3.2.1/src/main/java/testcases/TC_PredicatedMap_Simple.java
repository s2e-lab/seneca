package testcases;

import org.apache.commons.collections.map.PredicatedMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.HashMap;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_PredicatedMap_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_PredicatedMap_Simple tc = new TC_PredicatedMap_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        PredicatedMap object = (PredicatedMap) PredicatedMap.decorate(new HashMap(), null, null);
        object.put( "test", "test1" );
        return object;
    }

}
