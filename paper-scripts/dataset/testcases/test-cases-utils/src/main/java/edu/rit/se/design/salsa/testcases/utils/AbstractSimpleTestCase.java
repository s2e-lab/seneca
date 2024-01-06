package edu.rit.se.design.salsa.testcases.utils;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public abstract class AbstractSimpleTestCase extends AbstractTestCase {

    protected String getTestName() {
        return getClass().getName();
    }

}
