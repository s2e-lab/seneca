package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import javax.help.TreeItem;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_TreeItem_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_TreeItem_Simple tc = new TC_TreeItem_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        TreeItem object = new TreeItem();
        object.toString();
        return object;
    }

}
