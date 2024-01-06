package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;

/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_AttributeMap_SerializableRectangle2D_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_AttributeMap_SerializableRectangle2D_Map tc = new
                TC_AttributeMap_SerializableRectangle2D_Map(
                new TC_AttributeMap_SerializableRectangle2D_Simple()
        );
        tc.runTest();
    }

    public TC_AttributeMap_SerializableRectangle2D_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
