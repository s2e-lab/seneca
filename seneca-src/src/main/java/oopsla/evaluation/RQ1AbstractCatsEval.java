package oopsla.evaluation;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.types.MethodReference;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.serializer.JavaCallGraphSerializer;
import edu.rit.se.design.dodo.utils.viz.GraphVisualizer;
import oopsla.evaluation.utils.EvaluationUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.ibm.wala.types.ClassLoaderReference.Application;
import static com.ibm.wala.types.ClassLoaderReference.Primordial;
import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;
import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.nCFA;
import static java.lang.String.format;
import static java.lang.String.join;
import static oopsla.evaluation.utils.CATSTestCases.*;
import static oopsla.evaluation.utils.EvaluationUtil.checkDirectCall;
import static oopsla.evaluation.utils.EvaluationUtil.createMethodRef;

/**
 * Evaluates a call graph construction algorithm using the Call Graph & Assessment Suite (CATS).
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public abstract class RQ1AbstractCatsEval {
    private final String outputFolder;
    private final Map<String, Pair<MethodReference, MethodReference>> expectedResults;
    private final String approachName;
    private final PointerAnalysisPolicy[] taintedPolicies = new PointerAnalysisPolicy[]{
            new PointerAnalysisPolicy(ZeroXCFA, 1),
            new PointerAnalysisPolicy(nCFA, 1),
    };

    private static StringBuilder cgStats = new StringBuilder();

    protected RQ1AbstractCatsEval(String outputFolder, String approachName) {
        this.outputFolder = outputFolder;
        this.approachName = approachName;
        this.expectedResults = new HashMap<>();
        expectedResults.put(CASE_STUDY_SER1,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "writeObject", "(Ljava/io/ObjectOutputStream;)V"),
                        createMethodRef(Primordial, "Ljava/io/ObjectOutputStream", "defaultWriteObject", "()V")
                )
        );
        expectedResults.put(CASE_STUDY_SER2,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "writeObject", "(Ljava/io/ObjectOutputStream;)V"),
                        createMethodRef(Primordial, "Ljava/io/ObjectOutputStream", "defaultWriteObject", "()V")
                )
        );
        expectedResults.put(CASE_STUDY_SER3,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "writeObject", "(Ljava/io/ObjectOutputStream;)V"),
                        createMethodRef(Primordial, "Ljava/io/ObjectOutputStream", "defaultWriteObject", "()V")
                )
        );
        expectedResults.put(CASE_STUDY_SER4,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "readObject", "(Ljava/io/ObjectInputStream;)V"),
                        createMethodRef(Primordial, "Lsalsa/model/ObjectInputStream", "defaultReadObject", "()V")
                )
        );
        expectedResults.put(CASE_STUDY_SER5,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "readObject", "(Ljava/io/ObjectInputStream;)V"),
                        createMethodRef(Primordial, "Lsalsa/model/ObjectInputStream", "defaultReadObject", "()V")

                )
        );
        expectedResults.put(CASE_STUDY_SER6,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "writeReplace", "()Ljava/lang/Object;"),
                        createMethodRef(Application, "Lser/Demo", "replace", "()Ljava/lang/Object;")
                )
        );
        expectedResults.put(CASE_STUDY_SER7,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "readResolve", "()Ljava/lang/Object;"),
                        createMethodRef(Application, "Lser/Demo", "replace", "()Ljava/lang/Object;")
                )
        );
        expectedResults.put(CASE_STUDY_SER8,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Demo", "validateObject", "()V"),
                        createMethodRef(Application, "Lser/Demo", "callback", "()V")
                )
        );
        expectedResults.put(CASE_STUDY_SER9,
                new ImmutablePair<>(
                        createMethodRef(Application, "Lser/Superclass", "<init>", "()V"),
                        createMethodRef(Application, "Lser/Superclass", "callback", "()V")
                )
        );
    }


    /**
     * Runs all the experiments for the CATS benchmark.
     */
    public void runCatsTests() throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
        // validation checks
        if (EvaluationUtil.TC_ROOT_FOLDER == null)
            throw new IllegalStateException("The test cases root folder is not set. Please set the `testcase_folder` environment variable.");
        if (EvaluationUtil.STATIC_CGS_FOLDER == null)
            throw new IllegalStateException("The static call graphs folder is not set. Please set the `static_cgs_folder` environment variable.");


        for (String projectPath : expectedResults.keySet()) {
            for (PointerAnalysisPolicy policy : taintedPolicies) {
                String projectName = FilenameUtils.getBaseName(projectPath).split("-")[0];
                String testName = join("_", projectName, approachName, policy.toString());
                CallGraph cg = computeCallGraph(projectPath, policy);
                Pair<MethodReference, MethodReference> edge = expectedResults.get(projectPath);
                // TODO: capture the test results so it can be saved later in the CgStats file
                checkDirectCall(cg, edge.getLeft(), edge.getRight());
                // TODO: pass the test results to the saveCallGraph method
                saveCallGraph(cg, projectName, policy);
            }
        }
    }


    /**
     * Saves the call graph for a given project and policy.
     * The call graph is saved both in a text file and dot file format.
     *
     * @param computedCg the call graph to be saved
     * @param sampleName a unique identifier for the test case
     * @param policy     the pointer analysis policy used to compute the call graph
     * @throws IOException if the file cannot be saved
     */
    private void saveCallGraph(CallGraph computedCg, String sampleName, PointerAnalysisPolicy policy) throws IOException {
        CallGraphStats.CGStats stats = CallGraphStats.getCGStats(computedCg);
        cgStats.append(format("%s\t%s\t%s\t%s\t%s\n", approachName, sampleName, policy, stats.getNNodes(), stats.getNEdges()));


        String txtFilename;
        try {
            // if the int parsing succeeds, test case represents a test from the JCG suite (CATS)
            int i = Integer.valueOf(sampleName.substring(3, 4));
            txtFilename = format("Ser%d-%s-%s", i, approachName, policy.toString());
        } catch (NumberFormatException ex) {
            // sample is not from JCG test suite
            txtFilename = format("%s-%s-%s", sampleName, approachName, policy.toString());
        }

        // saves in txt format
        File txtFile = new File(outputFolder, txtFilename + ".txt");
        JavaCallGraphSerializer cgSerializer = new JavaCallGraphSerializer();
        cgSerializer.save(computedCg, txtFile);

        // saves in dot format
        File outputDotFile = new File(outputFolder, txtFilename + ".dot");
        new GraphVisualizer<CGNode>(format("%s (%s %s)", sampleName, approachName, policy),
                GraphVisualizer.getDefaultCgNodeLabeller(),
                GraphVisualizer.getDefaultCgNodeHighlighter(),
                null,
                GraphVisualizer.getDefaultCgNodeRemover(computedCg))
                .generateVisualGraph(computedCg, outputDotFile);

    }


    public static void printAndSaveStats() throws IOException {
        System.out.println(cgStats.toString());
        // save call graph stats per test case
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-mmm");
        File txtStatsFile = new File(format("%s/CgStats-%s.txt", EvaluationUtil.CATS_STATIC_CGS_FOLDER, f.format(new Date())));
        cgStats.insert(0, "ProjectName\tTestCase\tApproachName\tPolicy\t# Nodes\t#Edges\n");
        FileUtils.write(txtStatsFile, cgStats.toString(), Charset.defaultCharset());
    }

    protected abstract CallGraph computeCallGraph(String projectPath, PointerAnalysisPolicy policy) throws IOException, ClassHierarchyException, CallGraphBuilderCancelException;


}
