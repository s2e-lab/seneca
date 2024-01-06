package edu.rit.se.design.salsa.testcases.utils;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class CompositeArrayTestCase extends AbstractCompositeTestCase {
    public CompositeArrayTestCase(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    @Override
    protected String getTestName() {
        return simpleTestCase.getTestName() + "_Array";
    }

    @Override
    protected Object getObject() throws Exception {
        Object[] object = new Object[1];
        object[0] = simpleTestCase.getObject();
        return object;
    }
}
