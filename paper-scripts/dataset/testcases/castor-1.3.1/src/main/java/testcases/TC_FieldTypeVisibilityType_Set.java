package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeSetTestCase;

/**
 * This is a test case for a complex object serialization as a Set
 */

public class TC_FieldTypeVisibilityType_Set extends CompositeSetTestCase {

    public static void main(String[] args) throws Exception {
        TC_FieldTypeVisibilityType_Set tc = new
                TC_FieldTypeVisibilityType_Set(
                new TC_FieldTypeVisibilityType_Simple()
        );
        tc.runTest();
    }

    public TC_FieldTypeVisibilityType_Set(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
