package edu.rit.se.design.callgraph.cli;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.MonitorUtil;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaNCFACallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaZeroXCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaZeroXContainerCallGraphBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.*;
import static edu.rit.se.design.callgraph.util.AnalysisUtils.*;

/**
 * Command line interface for edu.rit.se.design.callgraph.cli.Salsa.
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class Salsa extends AbstractCLI {

    public static void main(String[] args) throws ClassHierarchyException, CallGraphBuilderCancelException, IOException {
        new Salsa().runAnalysis(args);
    }

    @Override
    protected Pair<CallGraph, CallGraphBuilder> computeCallGraph(String jarFilePath, File exclusions, PointerAnalysisPolicy primaryPaPolicy, PointerAnalysisPolicy secondaryPolicy, MonitorUtil.IProgressMonitor monitor) throws IOException, ClassHierarchyException, CallGraphBuilderCancelException {
        Pair<CallGraphBuilder, AnalysisOptions> tuple = getCallGraphBuilder(jarFilePath, exclusions, primaryPaPolicy, secondaryPolicy, monitor);
        CallGraphBuilder builder = tuple.getLeft();
        AnalysisOptions options = tuple.getRight();
        return new ImmutablePair<>(builder.makeCallGraph(options, monitor), builder);
    }

    public static Pair<CallGraphBuilder, AnalysisOptions> getCallGraphBuilder(String jarFilePath, File exclusions, PointerAnalysisPolicy primaryPaPolicy, PointerAnalysisPolicy secondaryPolicy, MonitorUtil.IProgressMonitor monitor) throws IOException, ClassHierarchyException {
        AnalysisScope scope = makeAnalysisScope(jarFilePath, exclusions, null);
        IClassHierarchy cha = makeIClassHierarchy(scope);
        AnalysisOptions options = makeAnalysisOptions(scope, cha);
        AnalysisCache cache = makeAnalysisCache();
        CallGraphBuilder builder = null;
        if (primaryPaPolicy.policyType == ZeroXCFA)
            builder = SalsaZeroXCallGraphBuilder.make(scope, options, cache, cha, primaryPaPolicy.policyNumber, secondaryPolicy);
        else if (primaryPaPolicy.policyType == nCFA)
            builder = SalsaNCFACallGraphBuilder.make(scope, options, cache, cha, primaryPaPolicy.policyNumber, secondaryPolicy);
        else if (primaryPaPolicy.policyType == ZeroXContainerCFA)
            builder = SalsaZeroXContainerCallGraphBuilder.make(scope, options, cache, cha, primaryPaPolicy.policyNumber, secondaryPolicy);

        if (builder == null) throw new IllegalStateException("Appropriate call graph builder could not be created");
        return new ImmutablePair<>(builder, options);
    }
}
