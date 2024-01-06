package testcases;

import org.apache.log4j.Level;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;



/**
*
 * This is a test case for a simple object serialization
 */

public class TC_Level_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_Level_Simple tc = new TC_Level_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Level object = Level.ALL;
        object.toString();
        return object;
    }

}
