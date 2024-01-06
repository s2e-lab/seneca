package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaContextInterpreter;
import edu.rit.se.design.callgraph.dispatcher.IDispatcher;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;

public class SenecaZeroXContainerCallGraphBuilder extends SenecaZeroXCallGraphBuilder {

    protected SenecaZeroXContainerCallGraphBuilder(IMethod abstractRootMethod, AnalysisOptions options,
                                                   IAnalysisCacheView cache, PointerKeyFactory pointerKeyFactory, ContextSelector appContextSelector,
                                                   int primaryPolicyVal, ContextSelector delegatedAppContextSelector,
                                                   SSAContextInterpreter appContextInterpreter, SSAContextInterpreter delegatedAppContextInterpreter,
                                                   PointerAnalysisPolicy type, IDispatcher dispatcher) {
        super(abstractRootMethod, options, cache, pointerKeyFactory,
                appContextSelector, delegatedAppContextSelector,
                appContextInterpreter, delegatedAppContextInterpreter,
                new PointerAnalysisPolicy(ZeroXCFA, primaryPolicyVal), type, dispatcher);
    }

    @SuppressWarnings("rawtypes")
    public static CallGraphBuilder make(
            AnalysisScope scope,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            IClassHierarchy cha,
            PointerAnalysisPolicy type, IDispatcher dispatcher) {
        return new SenecaZeroXContainerCallGraphBuilder(
                Language.JAVA.getFakeRootMethod(cha, options, cache),
                options, cache,
                new DefaultPointerKeyFactory(),
                null, type.policyNumber,
                null,
                null, new SalsaContextInterpreter(options, cache),
                type, dispatcher);
    }

    @Override
    protected ContextSelector makeDefaultContextSelector(ContextSelector appContextSelector) {
        return makeZeroXContainerContextSelector(appContextSelector, true);
    }

}
