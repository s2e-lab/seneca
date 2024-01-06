package testcases;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.set.AbstractSerializableSetDecorator;
import org.apache.commons.collections.set.UnmodifiableSet;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


/**
 * This is a test case for a simple object serialization
 */

public class TC_AbstractSerializableSetDecorator_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractSerializableSetDecorator_Simple tc = new TC_AbstractSerializableSetDecorator_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Set set = new HashSet();
        set.add("Test");
        AbstractSerializableSetDecorator object = (AbstractSerializableSetDecorator) UnmodifiableSet.decorate(set);
        return object;
    }

}
