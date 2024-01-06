package edu.rit.se.design.callgraph.evaluation.rq1_rq2.soundness_precision;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.MonitorUtil;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaNCFACallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaZeroXCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaNCFACallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaZeroXCallGraphBuilder;
import edu.rit.se.design.callgraph.dispatcher.SerializationDispatcher;
import edu.rit.se.design.callgraph.evaluation.rq3.performance.PerformanceTest;
import edu.rit.se.design.callgraph.evaluation.utils.XCorpusTestCases;
import edu.rit.se.design.callgraph.serializer.ICallGraphSerializer;
import edu.rit.se.design.callgraph.serializer.JDynCallGraphSerializer;
import edu.rit.se.design.dodo.utils.viz.GraphVisualizer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;
import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.nCFA;
import static edu.rit.se.design.callgraph.evaluation.utils.TestUtilities.*;
import static edu.rit.se.design.callgraph.util.AnalysisUtils.*;
import static java.lang.String.format;
import static java.lang.String.join;

/**
 * It generates the static call graphs for the testcases created for the XCorpus dataset.
 * It is used to verify the soundness and precision of the computed call graphs.
 *
 * @author Joanna C. S. Santos
 */
public class XCorpusSoundnessTest {

    public static final StringBuilder cgStats = new StringBuilder();
    public static final StringBuilder timeStats = new StringBuilder();

    public static final String OUTPUT_FOLDER = STATIC_CGS_FOLDER;


    public static String[] XCORPUS_TCS = new String[]{
            XCorpusTestCases.BATIK_TC,
            XCorpusTestCases.CASTOR_TC,
//            XCorpusTestCases.COMMONS_COLLECTION_TC, //FIXME: OutOfMemoryError
//            XCorpusTestCases.HTMLUNIT_TC, //FIXME: taking forever
            XCorpusTestCases.JAMES_TC,
            XCorpusTestCases.JGRAPH_TC,
            XCorpusTestCases.JPF_TC,
            XCorpusTestCases.LOG4J_TC,
//            XCorpusTestCases.MEGAMEK_TC, //FIXME: taking forever
            XCorpusTestCases.OPENJMS_TC,
            XCorpusTestCases.POOKA_TC,
//            XCorpusTestCases.TOMCAT_TC, //FIXME: array out of bounds
//            XCorpusTestCases.WEKA_TC, //FIXME: GC overhead limit exceeded
            XCorpusTestCases.XALAN_TC,
            XCorpusTestCases.XERCES_TC
    };


    public enum Approach {SENECA, SALSA, WALA}

    public static PointerAnalysisPolicy[] policies = new PointerAnalysisPolicy[]{
            new PointerAnalysisPolicy(ZeroXCFA, 1),
            new PointerAnalysisPolicy(nCFA, 1)
    };

    @BeforeAll
    public static void printHeader() throws IOException {
        // cleans up directory before saving the call graphs
        for (String projectPath : XCORPUS_TCS) {
            String projectName = getProjectName(projectPath);
            String projectFolderPath = String.format("%s/%s", OUTPUT_FOLDER, projectName);
            File projectFolder = new File(projectFolderPath);
            if (projectFolder.exists())
                FileUtils.cleanDirectory(projectFolder);
        }
    }

    public static String getProjectName(String projectPath) {
        return FilenameUtils.getBaseName(projectPath).replace("-testcases", "");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-mmm");
        File txtFile = new File(format("%s/CgStats-%s.txt", OUTPUT_FOLDER, f.format(new Date())));
        cgStats.insert(0, "ProjectName\tTestCase\tApproachName\tPolicy\t# Nodes\t#Edges\n");
        FileUtils.write(txtFile, cgStats.toString(), Charset.defaultCharset());


        File timeStatsFile = new File(format("%s/TimeStats-%s.txt", OUTPUT_FOLDER, f.format(new Date())));
        timeStats.insert(0, "ProjectName\tTestCase\tApproachName\tPolicy\tTime (ms)\t#Extra Iterations\n");
        FileUtils.write(timeStatsFile, timeStats.toString(), Charset.defaultCharset());
    }

    @TestFactory
    public Collection<DynamicTest> soundnessTests() {

        Collection<DynamicTest> dynamicTests = new ArrayList<>();

        for (String projectPath : XCORPUS_TCS) {
            for (Approach algorithm : Approach.values()) {
                for (PointerAnalysisPolicy policy : policies) {
                    String projectName = getProjectName(projectPath);
                    String approachName = algorithm.toString();
                    String testName = join("_", projectName, approachName, policy.toString());
                    DynamicTest dTest = DynamicTest.dynamicTest(testName, () -> {
                        List<CallGraph> callGraphs = computeCallGraphs(projectPath, algorithm, approachName, policy);

                        // saves call graphs
                        for (CallGraph computedCg : callGraphs) {
                            saveCallGraph(OUTPUT_FOLDER, computedCg, policy, projectName, approachName);
                            CallGraphStats.CGStats stats = CallGraphStats.getCGStats(computedCg);
                            String testCaseFileId = computedCg.getEntrypointNodes().iterator().next().getMethod().getDeclaringClass().getName().toString();
                            cgStats.append(format("%s\t%s\t%s\t%s\t%s\t%s\n",
                                    projectName, testCaseFileId, approachName,
                                    policy, stats.getNNodes(), stats.getNEdges()));
                        }
                    });
                    dynamicTests.add(dTest);
                }
            }
        }
        return dynamicTests;
    }

    public List<CallGraph> computeCallGraphs(String projectPath, Approach approach, String approachName, PointerAnalysisPolicy policy) throws ClassHierarchyException, IOException {
        List<CallGraph> callGraphs = new ArrayList<>();
        // Basic Variables for Analysis
        // adjust exclusion file for the Xerces project
        File exclusions = projectPath.equals(XCorpusTestCases.XERCES_TC) ? new File(EXCLUSIONS_FILE_XERCES) : new File(EXCLUSIONS_FILE);
        File jarFile = new File(projectPath);
        File dependenciesFolder = new File(jarFile.getParent(), "lib/" + jarFile.getName().split("\\.")[0]);
        AnalysisScope scope = makeAnalysisScope(projectPath, exclusions, dependenciesFolder);
        IClassHierarchy cha = makeIClassHierarchy(scope);
        AnalysisOptions options = makeAnalysisOptions(scope, cha);
        AnalysisCache cache = makeAnalysisCache();

        Iterable<? extends Entrypoint> entrypoints = options.getEntrypoints();


        entrypoints.forEach(ep -> {
            // ignore entrypoints in libraries
            // only entrypoints on classes in our testcase package
            if (isTestCaseEntryPoint(ep)) {
                timeStats.append(new File(projectPath).getName() + "\t" + ep.getMethod().getDeclaringClass().getName() + "\t" + approachName + "\t" + policy );
                try {
                    options.setEntrypoints(Arrays.asList(ep));
                    CallGraphBuilder builder = getCallgraphBuilder(approach, policy, scope, options, cache, cha);
                    long begin = System.currentTimeMillis();
                    PerformanceTest.CustomMonitor m= new PerformanceTest.CustomMonitor();
                    CallGraph cg = builder.makeCallGraph(options,m );
                    long end = System.currentTimeMillis();
                    timeStats.append("\t" + (end - begin) +"\t"+m.getExtraIterations()+"\n");
                    callGraphs.add(cg);
                } catch (CallGraphBuilderCancelException e) {
                    e.printStackTrace();
                }
            }
        });
        return callGraphs;
    }

    public static CallGraphBuilder getCallgraphBuilder(Approach approach, PointerAnalysisPolicy primaryPolicy, AnalysisScope scope, AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha) {
        int primaryPolicyVal = primaryPolicy.policyNumber;
        PointerAnalysisPolicy secondaryPolicy = new PointerAnalysisPolicy(nCFA, 1);
        switch (approach) {
            case SENECA:
                switch (primaryPolicy.policyType) {
                    case ZeroXCFA:
                        return SenecaZeroXCallGraphBuilder.make(scope, options, cache, cha, primaryPolicyVal, secondaryPolicy, new SerializationDispatcher(cha));
                    case nCFA:
                        return SenecaNCFACallGraphBuilder.make(scope, options, cache, cha, primaryPolicyVal, secondaryPolicy, new SerializationDispatcher(cha));
                    default:
                        throw new IllegalArgumentException("Unsupported policy type " + primaryPolicy.policyType);
                }
            case SALSA:
                switch (primaryPolicy.policyType) {
                    case ZeroXCFA:
                        return SalsaZeroXCallGraphBuilder.make(scope, options, cache, cha, primaryPolicyVal, secondaryPolicy);
                    case nCFA:
                        return SalsaNCFACallGraphBuilder.make(scope, options, cache, cha, primaryPolicyVal, secondaryPolicy);
                    default:
                        throw new IllegalArgumentException("Unsupported policy type " + primaryPolicy.policyType);
                }
            case WALA:
                switch (primaryPolicy.policyType) {
                    case ZeroXCFA:
                        if (primaryPolicy.policyNumber == 1)
                            return Util.makeZeroOneCFABuilder(Language.JAVA, options, cache, cha, scope);
                    case nCFA:
                        return Util.makeNCFABuilder(primaryPolicyVal, options, cache, cha, scope);
                    default:
                        throw new IllegalArgumentException("Unsupported policy type " + primaryPolicy.policyType);
                }
        }
        return null;
    }

    private static void saveCallGraph(String outputFolder, CallGraph computedCg, PointerAnalysisPolicy policy, String sampleName, String approachName) throws IOException {
        String filepath = String.format("./target/%s_%s_%s.dot", sampleName, approachName, policy);
        File outputDotFile = new File(filepath);
        new GraphVisualizer<CGNode>("Call graph view for " + sampleName,
                GraphVisualizer.getDefaultCgNodeLabeller(),
                GraphVisualizer.getDefaultCgNodeHighlighter(),
                null,
                GraphVisualizer.getDefaultCgNodeRemover(computedCg))
                .generateVisualGraph(computedCg, outputDotFile);

        String mainClass = getSingleEntrypointMethod(computedCg)
                .getDeclaringClass()
                .getName()
                .toString()
                .substring(11);
        String projectName = sampleName.split("-")[0];
        String filename = String.format("%s-%s-%s.txt", approachName, policy.toString(), mainClass);
        File txtFile = new File(String.format("%s/%s/%s", outputFolder, projectName, filename));

        ICallGraphSerializer cgSerializer = new JDynCallGraphSerializer(); //new JavaCallGraphSerializer();
        cgSerializer.save(computedCg, txtFile);
    }

    private static IMethod getSingleEntrypointMethod(CallGraph cg) {
        return cg.getEntrypointNodes().iterator().next().getMethod();
    }

    private boolean isTestCaseEntryPoint(Entrypoint ep) {
        return ep.getMethod().getDeclaringClass().getName().toString().startsWith("Ltestcases/TC_");
    }

}
