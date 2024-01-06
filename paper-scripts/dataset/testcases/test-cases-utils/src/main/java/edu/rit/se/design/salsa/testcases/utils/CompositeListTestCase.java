package edu.rit.se.design.salsa.testcases.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class CompositeListTestCase extends AbstractCompositeTestCase {
    public CompositeListTestCase(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    @Override
    protected String getTestName() {
        return simpleTestCase.getTestName() + "_List";
    }

    @Override
    protected Object getObject() throws Exception {
        List object = new ArrayList();
        object.add(simpleTestCase.getObject());
        return object;
    }
}
