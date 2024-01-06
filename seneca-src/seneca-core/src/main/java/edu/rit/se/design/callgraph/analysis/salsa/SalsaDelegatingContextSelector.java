package edu.rit.se.design.callgraph.analysis.salsa;


import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.intset.IntSet;


/**
 * It controls the context selection policy via delegation.
 *
 * @author Reese A. Jones (raj8065@rit.edu)
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 * @see ContextSelector
 */
public class SalsaDelegatingContextSelector implements ContextSelector {

    private ContextSelector normalSelector;
    private ContextSelector modelSelector;
    private SalsaCallGraphBuilder builder;

    /**
     * @param builder
     * @param normalSelector the context selector applied to method calls whose target are *NOT* de/serialization method models
     * @param modelSelector  the context selector applied to method calls whose target ARE de/serialization method models
     */
    public SalsaDelegatingContextSelector(SalsaCallGraphBuilder builder, ContextSelector normalSelector, ContextSelector modelSelector) {

        if (normalSelector == null)
            throw new IllegalArgumentException("normalSelector for ContextInterpreter delegation is null");
        if (modelSelector == null)
            throw new IllegalArgumentException("modelSelector for ContextInterpreter delegation is null");
        if (builder == null)
            throw new IllegalArgumentException("Builder for ContextInterpreter delegation is null");

        this.builder = builder;
        this.normalSelector = normalSelector;
        this.modelSelector = modelSelector;
    }

    /**
     * Delegation-based implementation.
     * If the caller is a synthetic method, then it applies the modelSelector, otherwise, it applies the normalSelector (i.e., the normal underlying pointer-analysis policy).
     *
     * @param caller           the node making the invocation
     * @param site             call site
     * @param callee           the node corresponding to the invocation target
     * @param actualParameters the passes instance types
     * @return the {@link Context} to be used in this call.
     */
    @Override
    public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] actualParameters) {
        return builder.isSyntheticModel(caller) ?
                modelSelector.getCalleeTarget(caller, site, callee, actualParameters) :
                normalSelector.getCalleeTarget(caller, site, callee, actualParameters);
    }

    /**
     * Delegation-based implementation.
     * If the caller is a synthetic method, then it applies the modelSelector, otherwise, it applies the normalSelector (i.e., the normal underlying pointer-analysis policy).
     *
     * @param caller caller node
     * @param site callsite
     * @return
     */
    @Override
    public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
        return builder.isSyntheticModel(caller) ?
                modelSelector.getRelevantParameters(caller, site) :
                normalSelector.getRelevantParameters(caller, site);
    }
}
