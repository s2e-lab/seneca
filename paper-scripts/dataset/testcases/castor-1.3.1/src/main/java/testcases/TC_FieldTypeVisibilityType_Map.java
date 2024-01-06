package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_FieldTypeVisibilityType_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_FieldTypeVisibilityType_Map tc = new
                TC_FieldTypeVisibilityType_Map(
                new TC_FieldTypeVisibilityType_Simple()
        );
        tc.runTest();
    }

    public TC_FieldTypeVisibilityType_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}