package testcases;

import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
import edu.rit.se.design.salsa.testcases.utils.CompositeListTestCase;

public class TC_CompositeRule_List extends CompositeListTestCase {


    public TC_CompositeRule_List(AbstractSimpleTestCase simpleTestCase) {
        super(simpleTestCase);
    }

    public static void main(String[] args) throws Exception {
        TC_CompositeRule_List tc = new
                TC_CompositeRule_List(
                new TC_CompositeRule_Simple()
        );
        tc.runTest();
    }
}
