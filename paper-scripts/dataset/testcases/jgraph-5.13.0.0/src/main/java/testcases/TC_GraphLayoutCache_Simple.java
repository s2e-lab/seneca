package testcases;

import org.jgraph.graph.GraphLayoutCache;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_GraphLayoutCache_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_GraphLayoutCache_Simple tc = new TC_GraphLayoutCache_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        GraphLayoutCache object = new GraphLayoutCache();

        return object;
    }

}
