package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;
/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_FileRevisionDO_Array extends CompositeArrayTestCase {

    public static void main(String[] args) throws Exception {
        TC_FileRevisionDO_Array tc = new
                TC_FileRevisionDO_Array(
                        new TC_FileRevisionDO_Simple()
        );
        tc.runTest();
    }

    public TC_FileRevisionDO_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
