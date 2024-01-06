package edu.rit.se.design.callgraph.analysis.seneca.nonprimordial;

import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DelegatingContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.salsa.SalsaContextInterpreter;
import edu.rit.se.design.callgraph.dispatcher.IDispatcher;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;


/**
 * This class builds call graphs using a 0-1-CFA algorithm.
 * It is similar to {@link ZeroXCFABuilder} class but with taint-based construction enabled
 * (ie, subclassing {@link BoundedSenecaCallGraphBuilder} instead of {@link SSAPropagationCallGraphBuilder}).
 *
 * @author Joanna C. S. Santos
 */
public class BoundedSenecaZeroOneCFACallGraphBuilder extends BoundedSenecaCallGraphBuilder {

    /**
     *
     * @param abstractRootMethod
     * @param options
     * @param cache
     * @param pointerKeyFactory
     * @param appContextSelector
     * @param delegatedAppContextSelector
     * @param appContextInterpreter
     * @param delegatedAppContextInterpreter
     * @param taintedPaPolicy
     * @param dispatcher
     */
    public BoundedSenecaZeroOneCFACallGraphBuilder(
            IMethod abstractRootMethod,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            PointerKeyFactory pointerKeyFactory,
            ContextSelector appContextSelector,
            ContextSelector delegatedAppContextSelector,
            SSAContextInterpreter appContextInterpreter,
            SSAContextInterpreter delegatedAppContextInterpreter,
            PointerAnalysisPolicy taintedPaPolicy,
            IDispatcher dispatcher) {
        super(abstractRootMethod,
                options,
                cache,
                pointerKeyFactory,
                appContextSelector,
                delegatedAppContextSelector,
                appContextInterpreter,
                delegatedAppContextInterpreter,
                new PointerAnalysisPolicy(ZeroXCFA, 1),
                taintedPaPolicy,
                dispatcher);

    }

    /**
     * @param options options that govern call graph construction
     * @param cha     governing class hierarchy
     * @param scope   representation of the analysis scope
     * @return a 0-1-CFA Call Graph Builder.
     * @throws IllegalArgumentException if options is null
     */
    public static SSAPropagationCallGraphBuilder make(AnalysisScope scope,
                                                      AnalysisOptions options,
                                                      IAnalysisCacheView cache,
                                                      IClassHierarchy cha,
                                                      PointerAnalysisPolicy taintedPaPolicy,
                                                      IDispatcher dispatcher) {

        if (options == null) {
            throw new IllegalArgumentException("options is null");
        }
        Util.addDefaultSelectors(options, cha);
        Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);

        return new BoundedSenecaZeroOneCFACallGraphBuilder(
                Language.JAVA.getFakeRootMethod(cha, options, cache),
                options,
                cache,
                new DefaultPointerKeyFactory(),
                null,
                null,
                null,
                new SalsaContextInterpreter(options, cache), //FIXME: why this interpreter?
                taintedPaPolicy,
                dispatcher
        );
    }

    /**
     * Makes the same context selector as  {@link ZeroXCFABuilder}.
     *
     * @param appContextSelector
     * @return The ContextSelector for ZeroX
     */
    @Override
    protected ContextSelector makeDefaultContextSelector(ContextSelector appContextSelector) {
        ContextSelector def = new DefaultContextSelector(options, cha);
        ContextSelector contextSelector =
                appContextSelector == null ? def : new DelegatingContextSelector(appContextSelector, def);
        return contextSelector;
    }

    /**
     * Makes the same context interpreter as  {@link ZeroXCFABuilder}.
     *
     * @param appContextInterpreter
     * @return The SSAContextInterpreter for ZeroX
     */
    @Override
    protected SSAContextInterpreter makeDefaultContextInterpreter(SSAContextInterpreter appContextInterpreter) {
        SSAContextInterpreter c = new DefaultSSAInterpreter(options, cache);
        c =
                new DelegatingSSAContextInterpreter(
                        ReflectionContextInterpreter.createReflectionContextInterpreter(
                                cha, options, getAnalysisCache()),
                        c);
        SSAContextInterpreter contextInterpreter =
                appContextInterpreter == null
                        ? c
                        : new DelegatingSSAContextInterpreter(appContextInterpreter, c);
        return contextInterpreter;
    }

    @Override
    protected InstanceKeyFactory makeDefaultMainInstanceKeyFactory() {
        int instanceKeysPolicy = ZeroXInstanceKeys.ALLOCATIONS
                /*| ZeroXInstanceKeys.SMUSH_MANY */
                | ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS
                | ZeroXInstanceKeys.SMUSH_STRINGS
                | ZeroXInstanceKeys.SMUSH_THROWABLES;
        return new ZeroXInstanceKeys(options, cha, getContextInterpreter(), instanceKeysPolicy);
    }
}
