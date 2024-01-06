package testcases;

import org.apache.commons.collections.set.UnmodifiableSortedSet;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.TreeSet;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_UnmodifiableSortedSet_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_UnmodifiableSortedSet_Simple tc = new TC_UnmodifiableSortedSet_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        TreeSet set = new TreeSet();
        set.add("test");
        UnmodifiableSortedSet object = (UnmodifiableSortedSet) UnmodifiableSortedSet.decorate(set);
        return object;
    }

}
