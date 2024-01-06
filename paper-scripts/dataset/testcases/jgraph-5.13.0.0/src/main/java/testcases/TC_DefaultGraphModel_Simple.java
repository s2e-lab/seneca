package testcases;

import org.jgraph.graph.DefaultGraphModel;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_DefaultGraphModel_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_DefaultGraphModel_Simple tc = new TC_DefaultGraphModel_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception {
        DefaultGraphModel object = new DefaultGraphModel();

        return object;
    }

}
