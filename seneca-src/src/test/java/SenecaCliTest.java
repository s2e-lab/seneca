import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.callgraph.cli.Seneca;
import edu.rit.se.design.callgraph.evaluation.utils.XCorpusTestCases;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class SenecaCliTest {
//    public static void main(String[] args) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
//        Seneca.main(XCorpusTestCases.LOG4J_TC_ARGS);
//    }


    public static void main(String[] args) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
        String[] exampleArgs = {
                "-j", "/Users/joanna/Documents/Portfolio/GitHub/S2E-Lab/seneca/paper-scripts/dataset/sample-programs/OOPSLAPaperExample-JRE1.7.jar",
                "-o", "./target/OOPSLAPaperExample.json",
                "-f", "json",
                "--main-policy", "0-1-CFA",
                "--view-ui",
        };

        Seneca.main(exampleArgs);
    }

}
