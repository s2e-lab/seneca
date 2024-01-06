package testcases;

import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.collections.bag.UnmodifiableSortedBag;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnmodifiableSortedBag_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableSortedBag_Simple tc = new TC_UnmodifiableSortedBag_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        TreeBag treeBag = new TreeBag();
        treeBag.add( "test" );

        UnmodifiableSortedBag unmodifiableSortedBag = (UnmodifiableSortedBag) UnmodifiableSortedBag.decorate(treeBag);
        return unmodifiableSortedBag;
    }

}
