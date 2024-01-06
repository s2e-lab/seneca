package edu.rit.se.design.salsa.testcases.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class CompositeMapTestCase extends AbstractCompositeTestCase {
    public CompositeMapTestCase(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    @Override
    protected String getTestName() {
        return simpleTestCase.getTestName() + "_Map";
    }

    @Override
    protected Object getObject() throws Exception {
        Map object = new HashMap();
        object.put("test", simpleTestCase.getObject());
        return object;
    }
}
