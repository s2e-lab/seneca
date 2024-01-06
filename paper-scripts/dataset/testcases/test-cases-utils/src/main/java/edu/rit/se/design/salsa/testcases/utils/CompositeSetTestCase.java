package edu.rit.se.design.salsa.testcases.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class CompositeSetTestCase extends AbstractCompositeTestCase {
    public CompositeSetTestCase(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    @Override
    protected String getTestName() {
        return simpleTestCase.getTestName() + "_Set";
    }

    @Override
    protected Object getObject() throws Exception {
        Set object = new HashSet();
        object.add(simpleTestCase.getObject());
        return object;
    }
}
