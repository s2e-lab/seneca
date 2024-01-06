package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeMapTestCase;
/**
 * This is a test case for a complex object serialization as a Map
 */

public class TC_UnionPathIterator_Map extends CompositeMapTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnionPathIterator_Map tc = new
                TC_UnionPathIterator_Map(
                        new TC_UnionPathIterator_Simple()
        );
        tc.runTest();
    }

    public TC_UnionPathIterator_Map(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

}
