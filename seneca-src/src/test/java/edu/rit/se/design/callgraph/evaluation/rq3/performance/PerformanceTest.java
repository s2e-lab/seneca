package edu.rit.se.design.callgraph.evaluation.rq3.performance;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefaultIRFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.IRFactory;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.MonitorUtil;
import com.ibm.wala.util.collections.HashSetFactory;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaNCFACallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaZeroXCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaNCFACallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaZeroXCallGraphBuilder;
import edu.rit.se.design.callgraph.dispatcher.SerializationDispatcher;
import edu.rit.se.design.callgraph.evaluation.rq1_rq2.soundness_precision.XCorpusSoundnessTest;
import edu.rit.se.design.callgraph.util.AnalysisUtils;
import edu.rit.se.design.dodo.utils.wala.WalaUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static com.ibm.wala.classLoader.Language.JAVA;
import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.nCFA;
import static edu.rit.se.design.callgraph.util.NameUtils.*;


public class PerformanceTest {
    //    private static String ROOT = "/Users/joanna/Documents/Portfolio/GitHub/pldi-2021-paper/xcorpus/";
    private static String ROOT = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/joannacss/seneca-scripts/dataset/xcorpus/";


    private static final String BATIK = ROOT + "batik-1.7/lib/batik-1.7.jar";
    private static final String CASTOR = ROOT + "castor-1.3.1/lib/castor-1.3.1.jar";
    private static final String JAMES = ROOT + "james-2.2.0/lib/james-2.2.0.jar";
    private static String JGRAPH = ROOT + "jgraphpad-5.10.0.2/lib/jgraphpad-5.10.0.2.jar";
    private static String JPF = ROOT + "jpf-1.5.1/lib/jpf-1.5.1.jar";
    private static final String LOG4J = ROOT + "log4j-1.2.16/lib/log4j-1.2.16.jar";
    private static final String OPENJMS = ROOT + "openjms-0.7.7-beta-1/lib/openjms-0.7.7-beta-1.jar";
    private static final String POOKA = ROOT + "pooka-3.0-080505/lib/pooka-3.0-080505.jar";
    private static final String XALAN = ROOT + "xalan-2.7.1/lib/xalan-2.7.1.jar";
    private static String WEKA = ROOT + "weka-3-7-9/lib/weka-3-7-9.jar";


    private static void computeCallGraph(String sample, CallGraphBuilder builder, AnalysisOptions options) throws CallGraphBuilderCancelException {
        long begin = System.currentTimeMillis();
        CustomMonitor customMonitor = new CustomMonitor();
        CallGraph cg = builder.makeCallGraph(options, customMonitor);
        long end = System.currentTimeMillis();
        System.out.println(builder.getClass().getSimpleName() + "\t" + new File(sample).getName() + "\t" + (end - begin) + "\t" + customMonitor.getExtraIterations() + "\t" + customMonitor.getDelta());
    }


    public static void main(String[] args) throws IOException, CallGraphBuilderCancelException, ClassHierarchyException {
        System.out.println("Sample\tTime (ms)\tExtra Iterations\tDelta");
        // Inputs
        String[] samples = XCorpusSoundnessTest.XCORPUS_TCS;//new String[]{BATIK, CASTOR, JAMES, JGRAPH, JPF, LOG4J, OPENJMS, POOKA, XALAN, WEKA};
        for (String sample : samples) {
            File exclusions = new File("Java60RegressionExclusions.txt");

            // Basic Variables
//            AnalysisScope scope = AnalysisUtils.makeAnalysisScope(sample, exclusions, new File(new File(sample).getParentFile().getAbsolutePath() + "/default-lib"));//makeAnalysisScope(sample, exclusions);
            AnalysisScope scope = AnalysisUtils.makeAnalysisScope(sample, exclusions,new File(new File(sample).getParent(), "lib/" + new File(sample).getName().split("\\.")[0]));
            IClassHierarchy cha = AnalysisUtils.makeIClassHierarchy(scope);
            AnalysisOptions options = makeAnalysisOptions(scope, cha);
            AnalysisCache cache = AnalysisUtils.makeAnalysisCache();

            // WALA 0-1-CFA
            computeCallGraph(sample, Util.makeZeroOneCFABuilder(JAVA, options, cache, cha, scope), options);
            // SALSA 0-1-CFA
            computeCallGraph(sample, SalsaZeroXCallGraphBuilder.make(scope, options, cache, cha, 1, new PointerAnalysisPolicy(nCFA, 1)), options);
            // SENECA 0-1-CFA
            SerializationDispatcher dispatcher = new SerializationDispatcher(cha);
            computeCallGraph(sample, SenecaZeroXCallGraphBuilder.make(scope, options, cache, cha, 1, new PointerAnalysisPolicy(nCFA, 1), dispatcher), options);


            // WALA 1-CFA
            computeCallGraph(sample, Util.makeNCFABuilder(1, options, cache, cha, scope), options);
            // SALSA 1-CFA
            computeCallGraph(sample, SalsaNCFACallGraphBuilder.make(scope, options, cache, cha, 1, new PointerAnalysisPolicy(nCFA, 1)), options);
            // SENECA 1-CFA
            computeCallGraph(sample, SenecaNCFACallGraphBuilder.make(scope, options, cache, cha, 1, new PointerAnalysisPolicy(nCFA, 1), dispatcher), options);

//            cg.stream().filter(n -> n.getMethod() instanceof MethodModel).collect(Collectors.toList());
//            new ProjectAnalysisViewer(cg, null, false).setTitle(sample);
        }


    }


    public static class CustomMonitor implements MonitorUtil.IProgressMonitor {
        private boolean isCanceled = false;
        private int extraIterations = 0;
        private int delta = 0;

        @Override
        public void beginTask(String s, int i) {
//            System.out.println("begin task " + s + " / i = " + i);
        }

        @Override
        public void subTask(String s) {
//            System.out.println("sub task " + s);
            if (s.contains("extra iterations")) {
                String extra = s.replace("SerializationPointsToSolver::extra iterations= ", "");
                extraIterations = Integer.parseInt(extra);
            }
            if (s.contains("delta")) {
                String extra = s.replace("SerializationPointsToSolver::delta= ", "");
                delta = Integer.parseInt(extra);
            }
        }

        @Override
        public void cancel() {
//            System.out.println("cancel");
            isCanceled = true;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public void done() {
//            System.out.println("done");
        }

        @Override
        public void worked(int i) {
//            System.out.println("worked i = " + i);
        }

        @Override
        public String getCancelMessage() {
            return "¯\\_(ツ)_/¯";
        }

        public int getExtraIterations() {
            return extraIterations;
        }

        public int getDelta() {
            return delta;
        }
    }
//<editor-fold desc="Basic Variable Construction">

    public static AnalysisOptions makeAnalysisOptions(AnalysisScope scope, IClassHierarchy cha) {
        Iterable<Entrypoint> entrypoints =
                Util.makeMainEntrypoints(scope, cha);
//        new AllApplicationEntrypoints(scope, cha);
//        computeMagicMethodEntrypoints(scope, cha);


        AnalysisOptions options = new AnalysisOptions();
        options.setEntrypoints(entrypoints);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        return options;
    }

    /**
     * This method finds all public methods
     *
     * @param cha
     * @return
     */
    public static Iterable<Entrypoint> computeMagicMethodEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
        final HashSet<Entrypoint> result = HashSetFactory.make();
        IRFactory<IMethod> irFactory = new DefaultIRFactory();

        for (IClass klass : cha) {
            if (!klass.isInterface()) {
                if (WalaUtils.isApplicationScope(klass)) {
                    for (IMethod method : klass.getDeclaredMethods()) {
                        if (!method.isAbstract() && method.isPublic()) {
                            result.add(new DefaultEntrypoint(method, cha));
//                            IR ir = irFactory.makeIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
//                            ir.iterateCallSites().forEachRemaining(callSite -> {
//                                MethodReference declaredTarget = callSite.getDeclaredTarget();
//                                TypeReference typeRef = declaredTarget.getDeclaringClass();
//                                Selector targetSelector = declaredTarget.getSelector();
//                                if ((typeRef.equals(JavaIoObjectInputStream) && targetSelector.equals(readObjectSelector)) ||
//                                        (typeRef.equals(JavaIoObjectOutputStream) && targetSelector.equals(writeObjectSelector))) {
//                                    result.add(new DefaultEntrypoint(method, cha));
//                                }
//                            });
                        }
                    }
                }
            }
        }
        Util.makeMainEntrypoints(scope, cha).forEach(result::add);
        return result;
    }

//</editor-fold>

}