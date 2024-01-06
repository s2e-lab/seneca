package edu.rit.se.design.salsa.testcases.utils;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public abstract class AbstractCompositeTestCase extends AbstractTestCase {
    AbstractSimpleTestCase simpleTestCase;

    AbstractCompositeTestCase(AbstractSimpleTestCase simpleTestCase) {
        this.simpleTestCase = simpleTestCase;
    }

}
