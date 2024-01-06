package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeArrayTestCase;

/**
 * This is a test case for a complex object serialization as an Array
 */

public class TC_AbstractSerializableListDecorator_Array extends CompositeArrayTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractSerializableListDecorator_Array tc = new
                TC_AbstractSerializableListDecorator_Array(
                        new TC_AbstractSerializableListDecorator_Simple()
        );
        tc.runTest();
    }

    public TC_AbstractSerializableListDecorator_Array(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
