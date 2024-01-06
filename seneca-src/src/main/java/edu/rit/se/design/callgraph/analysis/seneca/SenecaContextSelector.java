package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.intset.IntSet;


public class SenecaContextSelector implements ContextSelector {

    private ContextSelector primarySelector;
    private ContextSelector taintedSelector;
    private ITaintedCallGraphBuilder builder;

    public SenecaContextSelector(ITaintedCallGraphBuilder builder, ContextSelector primarySelector, ContextSelector taintedSelector) {
        if (primarySelector == null)
            throw new IllegalArgumentException("Untainted ContextSelector for ContextInterpreter delegation is null");
        if (taintedSelector == null)
            throw new IllegalArgumentException("Tainted ContextSelector for ContextInterpreter delegation is null");
        if (builder == null)
            throw new IllegalArgumentException("Builder for ContextSelector delegation is null");

        this.builder = builder;
        this.primarySelector = primarySelector;
        this.taintedSelector = taintedSelector;
    }

    @Override
    public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] actualParameters) {
        if (!builder.isTainted(caller))
            return primarySelector.getCalleeTarget(caller, site, callee, actualParameters);

        return taintedSelector.getCalleeTarget(caller, site, callee, actualParameters);
    }

    @Override
    public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
        if (!builder.isTainted(caller))
            return primarySelector.getRelevantParameters(caller, site);

        return taintedSelector.getRelevantParameters(caller, site);
    }
}
