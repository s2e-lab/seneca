package testcases;

import org.apache.commons.collections.list.CursorableLinkedList;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
 * This is a test case for a simple object serialization
 */

public class TC_CursorableLinkedList_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_CursorableLinkedList_Simple tc = new TC_CursorableLinkedList_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        CursorableLinkedList object = new CursorableLinkedList();
        object.add( "test" );
        return object;
    }

}