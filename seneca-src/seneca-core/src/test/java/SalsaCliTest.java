import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.callgraph.cli.Salsa;
import edu.rit.se.design.callgraph.evaluation.utils.XCorpusTestCases;

import java.io.IOException;

public class SalsaCliTest {

    public static void main(String[] args) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
        Salsa.main(XCorpusTestCases.LOG4J_TC_ARGS);
    }

}