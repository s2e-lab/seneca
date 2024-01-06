package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_AbstractSerializableSetDecorator_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractSerializableSetDecorator_Set tc = new
                TC_AbstractSerializableSetDecorator_Set(
                        new TC_AbstractSerializableSetDecorator_Simple()
        );
        tc.runTest();
    }

    public TC_AbstractSerializableSetDecorator_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
