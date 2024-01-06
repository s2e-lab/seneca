package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.FieldReference;

import java.util.Iterator;


public class SenecaContextInterpreter implements SSAContextInterpreter {

    private ITaintedCallGraphBuilder builder;
    private SSAContextInterpreter defaultInterpreter;
    private SSAContextInterpreter taintedInterpreter;

    public SenecaContextInterpreter(ITaintedCallGraphBuilder builder, SSAContextInterpreter defaultInterpreter, SSAContextInterpreter taintedInterpreter) {

        if (defaultInterpreter == null)
            throw new IllegalArgumentException("Untainted ContextInterpreter for ContextInterpreter delegation is null");
        if (taintedInterpreter == null)
            throw new IllegalArgumentException("Tainted ContextInterpreter for ContextInterpreter delegation is null");
        if (builder == null)
            throw new IllegalArgumentException("Builder for ContextInterpreter delegation is null");

        this.builder = builder;
        this.defaultInterpreter = defaultInterpreter;
        this.taintedInterpreter = taintedInterpreter;
    }

    @Override
    public Iterator<NewSiteReference> iterateNewSites(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.iterateNewSites(node);

        return taintedInterpreter.iterateNewSites(node);
    }

    @Override
    public Iterator<FieldReference> iterateFieldsRead(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.iterateFieldsRead(node);

        return taintedInterpreter.iterateFieldsRead(node);
    }

    @Override
    public Iterator<FieldReference> iterateFieldsWritten(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.iterateFieldsWritten(node);

        return taintedInterpreter.iterateFieldsWritten(node);
    }

    @Override
    public boolean recordFactoryType(CGNode node, IClass klass) {
        if (!builder.isTainted(node))
            return defaultInterpreter.recordFactoryType(node, klass);

        return taintedInterpreter.recordFactoryType(node, klass);
    }

    @Override
    public boolean understands(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.understands(node);

        return taintedInterpreter.understands(node);
    }

    @Override
    public Iterator<CallSiteReference> iterateCallSites(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.iterateCallSites(node);

        return taintedInterpreter.iterateCallSites(node);
    }

    @Override
    public IR getIR(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.getIR(node);

        return taintedInterpreter.getIR(node);
    }

    @Override
    public IRView getIRView(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.getIRView(node);

        return taintedInterpreter.getIRView(node);
    }

    @Override
    public DefUse getDU(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.getDU(node);

        return taintedInterpreter.getDU(node);
    }

    @Override
    public int getNumberOfStatements(CGNode node) {
        if (!builder.isTainted(node))
            return defaultInterpreter.getNumberOfStatements(node);

        return taintedInterpreter.getNumberOfStatements(node);
    }

    @Override
    public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getCFG(CGNode n) {
        if (!builder.isTainted(n))
            return defaultInterpreter.getCFG(n);

        return taintedInterpreter.getCFG(n);
    }
}
