package testcases;

import org.jgraph.JGraph;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_JGraph_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_JGraph_Simple tc = new TC_JGraph_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        JGraph object = new JGraph();

        return object;
    }

}
