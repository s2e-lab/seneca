package testcases;

import org.jgraph.graph.AttributeMap;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_AttributeMap_SerializablePoint2D_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AttributeMap_SerializablePoint2D_Simple tc = new TC_AttributeMap_SerializablePoint2D_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        AttributeMap.SerializablePoint2D object = new AttributeMap.SerializablePoint2D(0.0, 5.5);

        return object;
    }

}
