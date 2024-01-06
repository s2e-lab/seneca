package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaContextInterpreter;
import edu.rit.se.design.callgraph.dispatcher.IDispatcher;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;

public class SenecaZeroXCallGraphBuilder extends SenecaCallGraphBuilder {

    protected SenecaZeroXCallGraphBuilder(
            IMethod abstractRootMethod,
            AnalysisOptions options, IAnalysisCacheView cache,
            PointerKeyFactory pointerKeyFactory,
            ContextSelector appContextSelector, int primaryPolicyVal, ContextSelector delegatedAppContextSelector,
            SSAContextInterpreter appContextInterpreter, SSAContextInterpreter delegatedAppContextInterpreter,
            PointerAnalysisPolicy taintedPaPolicy, IDispatcher dispatcher) {
        super(abstractRootMethod, options, cache, pointerKeyFactory,
                appContextSelector, delegatedAppContextSelector,
                appContextInterpreter, delegatedAppContextInterpreter,
                new PointerAnalysisPolicy(ZeroXCFA, primaryPolicyVal), taintedPaPolicy, dispatcher);
    }

    // This constructor is used for ZeroXContainer.super call
    protected SenecaZeroXCallGraphBuilder(IMethod abstractRootMethod, AnalysisOptions options, IAnalysisCacheView cache,
                                          PointerKeyFactory pointerKeyFactory, ContextSelector appContextSelector,
                                          ContextSelector delegatedAppContextSelector, SSAContextInterpreter appContextInterpreter,
                                          SSAContextInterpreter delegatedAppContextInterpreter, PointerAnalysisPolicy PrimaryType, PointerAnalysisPolicy delegatedType, IDispatcher dispatcher) {
        super(abstractRootMethod, options, cache, pointerKeyFactory,
                appContextSelector, delegatedAppContextSelector,
                appContextInterpreter, delegatedAppContextInterpreter,
                PrimaryType, delegatedType, dispatcher);
    }

    @SuppressWarnings("rawtypes")
    public static CallGraphBuilder make(AnalysisScope scope,
                                        AnalysisOptions options,
                                        IAnalysisCacheView cache,
                                        IClassHierarchy cha,
                                        int val,
                                        PointerAnalysisPolicy type,
                                        IDispatcher dispatcher) {
        Util.addDefaultSelectors(options, cha);
        Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
        return new SenecaZeroXCallGraphBuilder(
                Language.JAVA.getFakeRootMethod(cha, options, cache),
                options, cache,
                new DefaultPointerKeyFactory(),
                null, val,
                null,
                null, new SalsaContextInterpreter(options, cache),
                type, dispatcher);
    }

    @Override
    protected ContextSelector makeDefaultContextSelector(ContextSelector appContextSelector) {
        return makeZeroXContextSelector(appContextSelector);
    }

    @Override
    protected SSAContextInterpreter makeDefaultContextInterpreter(SSAContextInterpreter appContextSelector) {
        return makeZeroXContextInterpreter(appContextSelector);
    }

    @Override
    protected InstanceKeyFactory makeDefaultMainInstanceKeyFactory() {
        return makeZeroXInstanceKeyFactory(defaultPaPolicy.policyNumber);
    }

}
