package testcases;

import org.apache.commons.collections.list.NodeCachingLinkedList;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_NodeCachingLinkedList_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_NodeCachingLinkedList_Simple tc = new TC_NodeCachingLinkedList_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        NodeCachingLinkedList object = new NodeCachingLinkedList();
        object.add( "test" );
        return object;
    }

}
