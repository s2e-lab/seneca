package edu.rit.se.design.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.strings.Atom;
import edu.rit.se.design.dodo.utils.viz.ProjectAnalysisViewer;
import edu.rit.se.design.callgraph.evaluation.utils.TestDataSets;
import edu.rit.se.design.callgraph.util.NameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static edu.rit.se.design.callgraph.util.AnalysisUtils.*;


/**
 * A class for exploring the idea of automatically creating API models that reflect the sequence of method calls soundly.
 * @author Joanna C. S. Santos
 */
public class ModelGenerator {


    public static AnalysisScope makeModelAnalysisScope(String jarFilePath, File exclusionFile) {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        ClassLoaderReference loader = scope.getLoader(AnalysisScope.APPLICATION);

        AnalysisScopeReader.addClassPathToScope(jarFilePath, scope, loader);
        return scope;
    }

    public static void main(String[] args) throws IOException, ClassHierarchyException, CancelException {

        String jarFilePath = TestDataSets.PROPOSAL_EXAMPLE;//"/Users/joanna/Google Drive/Research Assistant/Projects/Weaknesses/DODO/config/JREs/jre1.8_mini/lib/rt.jar";
        File exclusionFile = new File(ModelGenerator.class.getClassLoader().getResource(edu.rit.se.design.callgraph.evaluation.utils.TestUtilities.DEFAULT_EXCLUSIONS_FILE).toString());
        AnalysisScope scope = makeAnalysisScope(jarFilePath, exclusionFile,null); //makeModelAnalysisScope(jarFilePath, exclusionFile);
        IClassHierarchy cha = makeIClassHierarchy(scope);
        AnalysisOptions options = new AnalysisOptions();


        options.setEntrypoints(computeEntrypoints(NameUtils.JavaIoObjectInputStream, cha));
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        AnalysisCache cache = makeAnalysisCache();

        SSAPropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, cache, cha, scope);


        CallGraph cg = builder.makeCallGraph(options);


        Set<NormalStatement> reflectionCalls = new HashSet<>();

        for (CGNode cgNode : cg) {

            cgNode.iterateCallSites().forEachRemaining(csr -> {
                MethodReference declaredTarget = csr.getDeclaredTarget();

                TypeReference declaringClass = declaredTarget.getDeclaringClass();
                boolean isMethodClass = declaringClass.getName().equals(TypeName.string2TypeName("Ljava/lang/reflect/Method"));
                boolean isInvokeMethod = declaredTarget.getName().equals(Atom.findOrCreateAsciiAtom("invoke"));
                if (isMethodClass && isInvokeMethod) {
                    SSAAbstractInvokeInstruction[] calls = cgNode.getIR().getCalls(csr);
                    for (SSAAbstractInvokeInstruction call : calls) {
                        reflectionCalls.add(new NormalStatement(cgNode, call.iIndex()));
                    }
                }
            });
        }

//        for (NormalStatement source : reflectionCalls) {
//            Collection<Statement> srcSlice = Slicer.computeForwardSlice(source, cg, builder.getPointerAnalysis(), Slicer.DataDependenceOptions.NO_BASE_PTRS, Slicer.ControlDependenceOptions.FULL);
//
//            for (NormalStatement target : reflectionCalls) {
//                if (source == target) continue;
//
//
//                Collection<Statement> tgtSlice = Slicer.computeBackwardSlice(target, cg, builder.getPointerAnalysis(), Slicer.DataDependenceOptions.NO_BASE_NO_HEAP, Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);
//                Set<Statement> intersection = new HashSet<>(srcSlice);
//                intersection.retainAll(tgtSlice);
//
//
//                System.out.println("SOURCE: " + source.getInstruction() + "(slice size = " + srcSlice.size() + ")");
//                System.out.println("TARGET: " + target.getInstruction() + "(slice size = " + tgtSlice.size() + ")");
//                System.out.println("===================================");
//                intersection.forEach(chopStmt -> System.out.println("\t" + chopStmt));
//
//
//            }
//
//
//        }


        SDG sdg = new SDG(cg, builder.getPointerAnalysis(), Slicer.DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS, Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES);
        Set<PointerKey> taintedPointers = new HashSet<>();
        Collection<Statement> statements = Slicer.computeBackwardSlice(sdg, new ArrayList<>(reflectionCalls));
        for (Statement statement : statements) {
            if (statement instanceof NormalStatement) {
                SSAInstruction instruction = ((NormalStatement) statement).getInstruction();
                if (instruction.hasDef()) {
                    PointerKey pointerKey = builder.getPointerKeyForLocal(statement.getNode(), instruction.getDef(0));
                    taintedPointers.add(pointerKey);
                }
            }
        }

//        for (NormalStatement reflectionCall : reflectionCalls) {
//            SSAInstruction instruction = reflectionCall.getInstruction();
//            if(instruction.hasDef()){
//                PointerKey pointerKey = builder.getPointerKeyForLocal(reflectionCall.getNode(), instruction.getDef(0));
//                taintedPointers.add(pointerKey);
//            }
//        }


        ProjectAnalysisViewer projectAnalysisViewer = new ProjectAnalysisViewer(cg, taintedPointers, false);


    }


    private static Set<Entrypoint> computeEntrypoints(TypeReference classToModel, IClassHierarchy cha) {
        final HashSet<Entrypoint> result = HashSetFactory.make();

        IClass klass = cha.lookupClass(classToModel);

        for (IMethod method : klass.getDeclaredMethods()) {
            if (method.isPublic() && !method.isStatic()) {
                if (method.getName().toString().equals("readObject"))
                    result.add(new DefaultEntrypoint(method, cha));
            }
        }

        return result;
    }
}
