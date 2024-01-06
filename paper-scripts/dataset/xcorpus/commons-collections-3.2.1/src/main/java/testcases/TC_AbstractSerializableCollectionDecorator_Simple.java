package testcases;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.collection.AbstractSerializableCollectionDecorator;
import org.apache.commons.collections.collection.PredicatedCollection;
import org.apache.commons.collections.functors.NotNullPredicate;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.util.ArrayList;


/**
 * This is a test case for a simple object serialization
 */

public class TC_AbstractSerializableCollectionDecorator_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_AbstractSerializableCollectionDecorator_Simple tc = new TC_AbstractSerializableCollectionDecorator_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        AbstractSerializableCollectionDecorator object = (PredicatedCollection) PredicatedCollection.decorate(new ArrayList(), NotNullPredicate.INSTANCE);
        object.add( "test" );
        return object;
    }

}
