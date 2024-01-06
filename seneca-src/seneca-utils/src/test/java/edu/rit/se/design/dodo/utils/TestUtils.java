package edu.rit.se.design.dodo.utils;


import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for testing purposes.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class TestUtils {

    // configuration for the SDG/Call Graph extractions
    private static final String EXCLUSIONS_FILE = ResourceLoader.getResourcePath("/config/exclusions/CustomJavaExclusions-DODO.txt");

    // data and control dependence options
    private static final Slicer.DataDependenceOptions DATA_DEPENDENCE_OPTION = Slicer.DataDependenceOptions.NO_BASE_NO_HEAP;
    private static final Slicer.ControlDependenceOptions CONTROL_DEPENDENCE_OPTION = Slicer.ControlDependenceOptions.FULL;

    // FIXME: replace this hardcoded path to the test resources folder with a relative path
    public static final String ROOT_TEST_ASSETS = "../../../DODO-TestData/";

    /**
     * It computes a {@link SDG}, {@link CallGraph}, {@link IClassHierarchy} and other meta data from a project's artifact.
     *
     * @param artifact the project folder containing the project's source code or the path to the project's WAR/JAR file.
     * @return the System Dependence Graph of the system
     * @throws IOException
     * @throws ClassHierarchyException
     * @throws IllegalArgumentException
     * @throws CallGraphBuilderCancelException
     */
    public static SDG<InstanceKey> extractSDG(String artifact) throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
        // exclusion file
        File exFile = (new FileProvider()).getFile(EXCLUSIONS_FILE);
        // gets an analysis scope on top of the source code
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(artifact, exFile);

        // build the class hierarchy
        IClassHierarchy cha = ClassHierarchyFactory.make(scope);

        // build entry points
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha);

        // builds call graph
        AnalysisOptions options = new AnalysisOptions();
        options.setEntrypoints(entrypoints);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
        AnalysisCache cache = new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory());
        CallGraphBuilder builder = Util.makeZeroOneContainerCFABuilder(options, cache, cha, scope);
        CallGraph cg = builder.makeCallGraph(options, null);

        // generates a SDG from the callgraph
        PointerAnalysis<InstanceKey> pa = (PointerAnalysis<InstanceKey>) builder.getPointerAnalysis();
        SDG<InstanceKey> sdg = new SDG<>(cg, pa, ModRef.<InstanceKey>make(), DATA_DEPENDENCE_OPTION, CONTROL_DEPENDENCE_OPTION);

        return sdg;
    }

}
