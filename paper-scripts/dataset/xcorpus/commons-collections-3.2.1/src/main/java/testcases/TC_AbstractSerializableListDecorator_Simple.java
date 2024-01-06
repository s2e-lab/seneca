package testcases;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.list.AbstractSerializableListDecorator;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.collections.functors.ConstantFactory;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.ArrayList;


/**
 * This is a test case for a simple object serialization
 */

public class TC_AbstractSerializableListDecorator_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractSerializableListDecorator_Simple tc = new TC_AbstractSerializableListDecorator_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
	ConstantFactory cFactory = new ConstantFactory("test");
        AbstractSerializableListDecorator object = (AbstractSerializableListDecorator) LazyList.decorate(new ArrayList<String>(), cFactory);
        object.add( "test" );
        return object;
    }

}
