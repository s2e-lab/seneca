import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.callgraph.cli.Seneca;
import edu.rit.se.design.callgraph.evaluation.utils.XCorpusTestCases;

import java.io.IOException;

/**
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class SenecaCliTest {
    public static void main(String[] args) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
        Seneca.main(XCorpusTestCases.LOG4J_TC_ARGS);
    }
}
