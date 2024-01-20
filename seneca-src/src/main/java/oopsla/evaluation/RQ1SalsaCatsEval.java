package oopsla.evaluation;

import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.cli.Salsa;
import oopsla.evaluation.utils.EvaluationUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

import static oopsla.evaluation.utils.EvaluationUtil.STATIC_CGS_FOLDER;
import static oopsla.evaluation.utils.EvaluationUtil.TC_ROOT_FOLDER;


/**
 * This class runs Salsa over the CATS dataset.
 * @author Joanna C. S. Santos (jds5109@rit.edu).
 */
public class RQ1SalsaCatsEval extends RQ1AbstractCatsEval {

    protected RQ1SalsaCatsEval() {
        super(EvaluationUtil.CATS_STATIC_CGS_FOLDER, "Salsa");
    }

    @Override
    protected CallGraph computeCallGraph(String sample, PointerAnalysisPolicy primaryPaPolicy) throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {
        File exclusions = new File(EvaluationUtil.EXCLUSIONS_FILE);
        // Basic Variables
        PointerAnalysisPolicy secondaryPolicy = new PointerAnalysisPolicy(PointerAnalysisPolicy.PolicyType.nCFA, 1);
        Pair<CallGraphBuilder, AnalysisOptions> tuple = Salsa.getCallGraphBuilder(sample, exclusions, primaryPaPolicy, secondaryPolicy, null);
        CallGraphBuilder builder = tuple.getLeft();
        AnalysisOptions options = tuple.getRight();
        return builder.makeCallGraph(options, null);
    }

    public static void main(String[] args) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
        if(TC_ROOT_FOLDER == null || STATIC_CGS_FOLDER == null) {
            throw new RuntimeException("Please provide the following program properties: " +
                    "\n\t-Dtestcase_folder=/path/to/cats/testcases" +
                    "\n\t-Dstatic_cgs_folder=/path/to/where/static/call/graphs/should/be/saved");
        }
        RQ1SalsaCatsEval eval = new RQ1SalsaCatsEval();
        eval.runCatsTests();
    }

}