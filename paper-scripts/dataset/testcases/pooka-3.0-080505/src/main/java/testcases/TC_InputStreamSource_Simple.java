package testcases;

import org.htmlparser.lexer.InputStreamSource;
import edu.rit.se.design.salsa.testcases.utils.AbstractSimpleTestCase;

import java.io.File;
import java.io.FileInputStream;


/**
*
 * This is a test case for a simple object serialization
 */

public class TC_InputStreamSource_Simple extends AbstractSimpleTestCase {

    public static void main(String[] args) throws Exception {
        TC_InputStreamSource_Simple tc = new TC_InputStreamSource_Simple();
        tc.runTest();

    }

    @Override
    protected Object getObject() throws Exception{
        File testFile = new File("test.txt");
        testFile.createNewFile();
        testFile.deleteOnExit();
        InputStreamSource object = new InputStreamSource( new FileInputStream(testFile));
        return object;
    }

}
