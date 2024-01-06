package testcases;

import com.ivata.groupware.container.persistence.BaseDO;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;
/**
 * This is a test case for a simple object serialization
 */

public class TC_BaseDO_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_BaseDO_Simple tc = new TC_BaseDO_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        BaseDO object = new BaseDO() {
            @Override
            public boolean equals(Object compare) {
                return super.equals(compare);
            }
        };
        return object;
    }

}
