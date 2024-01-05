package edu.rit.se.design.callgraph.evaluation.rq1_rq2.soundness_precision;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaCallGraphBuilder;
import edu.rit.se.design.callgraph.evaluation.utils.XCorpusTestCases;
import edu.rit.se.design.dodo.utils.viz.ProjectAnalysisViewer;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;
import static edu.rit.se.design.callgraph.evaluation.utils.TestUtilities.EXCLUSIONS_FILE;
import static edu.rit.se.design.callgraph.evaluation.utils.TestUtilities.EXCLUSIONS_FILE_XERCES;
import static edu.rit.se.design.callgraph.util.AnalysisUtils.*;

/**
 * This class is here merely to debug call graphs.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class XCorpusDebugging {

    public static void main(String[] args) throws Exception {
        // parameters for debugging
        String projectPath = XCorpusTestCases.BATIK_TC;
        String testcase = "Ltestcases/TC_TextNode_Anchor_Set";
        XCorpusSoundnessTest.Approach approach = XCorpusSoundnessTest.Approach.SENECA;
        PointerAnalysisPolicy paPolicy = new PointerAnalysisPolicy(ZeroXCFA, 1);

        // Basic Variables for Analysis
        File exclusions = new File(projectPath.equals(XCorpusTestCases.XERCES_TC) ?  EXCLUSIONS_FILE_XERCES : EXCLUSIONS_FILE);
        File jarFile = new File(projectPath);
        File dependenciesFolder = new File(jarFile.getParent(), "lib/" + jarFile.getName().split("\\.")[0]);
        AnalysisScope scope = makeAnalysisScope(projectPath, exclusions, dependenciesFolder);
        IClassHierarchy cha = makeIClassHierarchy(scope);
        AnalysisOptions options = makeAnalysisOptions(scope, cha,false);
        AnalysisCache cache = makeAnalysisCache();
        Iterable<? extends Entrypoint> entrypoints = options.getEntrypoints();
        CallGraphBuilder builder = null;
        // compute call graphs
        CallGraph cg = null;

        for (Entrypoint ep : entrypoints) {
            if (ep.getMethod().getDeclaringClass().getName().toString().startsWith(testcase)) {
                System.out.println(new File(projectPath).getName() + " " + ep.getMethod().getDeclaringClass().getName() + " " + approach);
                try {
                    options.setEntrypoints(Arrays.asList(ep));
                    builder = XCorpusSoundnessTest.getCallgraphBuilder(approach, paPolicy, scope, options, cache, cha);
                    cg = builder.makeCallGraph(options, null);
                    break;
                } catch (CallGraphBuilderCancelException e) {
                    e.printStackTrace();
                }
            }
        }

        // visualizes call graph
        boolean prune = false;
        Set<PointerKey> taintedPointers = null;
        if (builder instanceof SenecaCallGraphBuilder) {
            taintedPointers = ((SenecaCallGraphBuilder) builder).getTaintedPointers();
        }
        new ProjectAnalysisViewer(cg, taintedPointers, prune).setTitle(approach + " " + paPolicy + " " + testcase + " " + new File(projectPath).getName());


    }


}
