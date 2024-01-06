package edu.rit.se.design.callgraph.analysis.salsa;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AbstractRootMethod;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;

import static edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy.PolicyType.ZeroXCFA;


/**
 * @author Reese A. Jones (raj8065@rit.edu)
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */

public class SalsaZeroXCallGraphBuilder extends SalsaCallGraphBuilder {

    protected SalsaZeroXCallGraphBuilder(IMethod abstractRootMethod, AnalysisOptions options, IAnalysisCacheView cache,
                                         PointerKeyFactory pointerKeyFactory, ContextSelector appContextSelector, int primaryPolicyVal,
                                         ContextSelector delegatedAppContextSelector, SSAContextInterpreter appContextInterpreter,
                                         SSAContextInterpreter delegatedAppContextInterpreter, PointerAnalysisPolicy secondaryPaPolicy) {
        super(abstractRootMethod, options, cache, pointerKeyFactory,
                appContextSelector, delegatedAppContextSelector,
                appContextInterpreter, delegatedAppContextInterpreter,
                new PointerAnalysisPolicy(ZeroXCFA, primaryPolicyVal), secondaryPaPolicy);
    }

    // This constructor is used for ZeroXContainer.super call
    protected SalsaZeroXCallGraphBuilder(IMethod abstractRootMethod, AnalysisOptions options, IAnalysisCacheView cache,
                                         PointerKeyFactory pointerKeyFactory, ContextSelector appContextSelector,
                                         ContextSelector delegatedAppContextSelector, SSAContextInterpreter appContextInterpreter,
                                         SSAContextInterpreter delegatedAppContextInterpreter,
                                         PointerAnalysisPolicy primaryPaPolicy, PointerAnalysisPolicy secondaryPaPolicy) {
        super(abstractRootMethod, options, cache, pointerKeyFactory,
                appContextSelector, delegatedAppContextSelector,
                appContextInterpreter, delegatedAppContextInterpreter,
                primaryPaPolicy, secondaryPaPolicy);
    }

    @SuppressWarnings("rawtypes")
    public static CallGraphBuilder make(AnalysisScope scope, AnalysisOptions options, IAnalysisCacheView cache, IClassHierarchy cha, int primaryPolicyVal, PointerAnalysisPolicy secondaryPaPolicy) {
        Util.addDefaultSelectors(options, cha);
        Util.addDefaultBypassLogic(options, scope, Util.class.getClassLoader(), cha);
        PointerKeyFactory pointerKeyFactory = new DefaultPointerKeyFactory();
        AbstractRootMethod fakeRootMethod = Language.JAVA.getFakeRootMethod(cha, options, cache);

        return new SalsaZeroXCallGraphBuilder(fakeRootMethod, options, cache, pointerKeyFactory,
                null, primaryPolicyVal, null, null,
                null, secondaryPaPolicy);
    }

    @Override
    protected ContextSelector makePrimaryContextSelector(ContextSelector appContextSelector) {
        return makeZeroXContextSelector(appContextSelector);
    }

    @Override
    protected SSAContextInterpreter makePrimarySSAContextInterpreter(SSAContextInterpreter appContextSelector) {
        return makeZeroXContextInterpreter(appContextSelector);
    }

    @Override
    protected InstanceKeyFactory makePrimaryInstanceKeyFactory() {
        return makeZeroXInstanceKeyFactory(defaultPaPolicy.policyNumber);
    }

}
