package testcases;

import org.htmlparser.lexer.Page;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_LexerPage_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_LexerPage_Simple tc = new TC_LexerPage_Simple();
        tc.runTest();
    }

    @Override
    protected Object getObject() throws Exception{
        Page object = new Page();
        object.toString();
        return object;
    }

}
