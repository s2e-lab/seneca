package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.callgraph.propagation.rta.RTAContextInterpreter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaContextInterpreter;
import edu.rit.se.design.callgraph.dispatcher.IDispatcher;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.nCFA;

public class SenecaNCFACallGraphBuilder extends SenecaCallGraphBuilder {

    // When Util.makeNCFABuilder is called the instance key factory is overridden after the original nCFA builder is created.
    // This leaves us with two versions of nCFA. This boolean controls which version the tainted builder is based on.

    private boolean useUtilVersion;

    public SenecaNCFACallGraphBuilder(
            IMethod abstractRootMethod,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            PointerKeyFactory pointerKeyFactory,
            ContextSelector appContextSelector,
            ContextSelector delegatedAppContextSelector,
            SSAContextInterpreter appContextInterpreter,
            SSAContextInterpreter delegatedAppContextInterpreter,
            int val,
            PointerAnalysisPolicy delegatedType,
            boolean useUtilVersion, IDispatcher dispatcher) {
        super(abstractRootMethod,
                options, cache,
                pointerKeyFactory,
                appContextSelector,
                delegatedAppContextSelector,
                appContextInterpreter,
                delegatedAppContextInterpreter,
                new PointerAnalysisPolicy(nCFA, val),
                delegatedType,
                dispatcher);
        this.useUtilVersion = useUtilVersion;
    }

    @SuppressWarnings("rawtypes")
    private static CallGraphBuilder make(
            AnalysisScope scope,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            IClassHierarchy cha,
            int val,
            PointerAnalysisPolicy type,
            boolean UtilVersion, IDispatcher dispatcher) {
        Util.addDefaultSelectors(options, cha);
        Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
        return new SenecaNCFACallGraphBuilder(
                Language.JAVA.getFakeRootMethod(cha, options, cache),
                options, cache,
                new DefaultPointerKeyFactory(),
                null, null,
                null, new SalsaContextInterpreter(options, cache),
                val, type,
                UtilVersion, dispatcher);
    }

    @SuppressWarnings("rawtypes")
    public static CallGraphBuilder make(
            AnalysisScope scope,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            IClassHierarchy cha,
            int val,
            PointerAnalysisPolicy type, IDispatcher dispatcher) {
        // Auto defaults to Util.makeNCFA version
        return make(scope, options, cache, cha, val, type, true, dispatcher);
    }

    @Override
    protected ContextSelector makeDefaultContextSelector(ContextSelector appContextSelector) {
        return this.makeNCFAContextSelector(appContextSelector, defaultPaPolicy.policyNumber);
    }

    @Override
    protected SSAContextInterpreter makeDefaultContextInterpreter(SSAContextInterpreter appContextSelector) {
        return this.makeNCFAContextInterpreter(appContextSelector);
    }

    @Override
    protected InstanceKeyFactory makeDefaultMainInstanceKeyFactory() {

        if (!useUtilVersion)
            return this.makeNCFAInstanceKeyFactory();
        else
            return makeSpecificUtilInstanceKeyFactory();
    }

    /**
     * Util.makeNCFA changes the InstanceKeyFactory used rather than using the default.
     * This method simulates making the NCFA the same as the Util method.
     *
     * @return
     */
    private InstanceKeyFactory makeSpecificUtilInstanceKeyFactory() {
        RTAContextInterpreter contextInterpreter = getContextInterpreter();
        if (contextInterpreter == null)
            throw new RuntimeException(" ContextInterpreter was null while making SpecificUtilInstanceKeyFactory");

        return new ZeroXInstanceKeys(
                options,
                cha,
                contextInterpreter,
                ZeroXInstanceKeys.ALLOCATIONS
                        | ZeroXInstanceKeys.SMUSH_MANY
                        | ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS
                        | ZeroXInstanceKeys.SMUSH_STRINGS
                        | ZeroXInstanceKeys.SMUSH_THROWABLES);
    }
}
