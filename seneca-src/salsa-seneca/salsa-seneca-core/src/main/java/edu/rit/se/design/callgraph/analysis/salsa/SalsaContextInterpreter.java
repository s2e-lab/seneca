package edu.rit.se.design.callgraph.analysis.salsa;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ContextInsensitiveSSAInterpreter;
import com.ibm.wala.ssa.*;
import edu.rit.se.design.callgraph.model.MethodModel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An object that provides an interface to local method information needed for CFA.
 * This context interpreter only understands nodes whose methods are a {@link MethodModel}.
 *
 * @author Joanna C. S. Santos
 */
public class SalsaContextInterpreter extends ContextInsensitiveSSAInterpreter {

    public SalsaContextInterpreter(AnalysisOptions options, IAnalysisCacheView cache) {
        super(options, cache);
    }

    @Override
    public boolean understands(CGNode node) {
        return node.getMethod() instanceof MethodModel;
    }

    @Override
    public IR getIR(CGNode node) {
        if (node == null) throw new IllegalArgumentException("node is null");
        return getAnalysisCache().getIR(node.getMethod(), node.getContext());
    }

    @Override
    public IRView getIRView(CGNode node) {
        return getIR(node);
    }

    @Override
    public int getNumberOfStatements(CGNode node) {
        IR ir = getIR(node);
        return (ir == null) ? -1 : ir.getInstructions().length;
    }

    @Override
    public boolean recordFactoryType(CGNode node, IClass klass) {
        return false;
    }

    @Override
    public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getCFG(CGNode N) {
        IR ir = getIR(N);
        return ir == null ? null : ir.getControlFlowGraph();
    }

    @Override
    public DefUse getDU(CGNode node) {
        if (node == null) {
            throw new IllegalArgumentException("node is null");
        }
        IR ir = getAnalysisCache().getIR(node.getMethod(), node.getContext());
        return ((AnalysisCache) getAnalysisCache()).getSSACache().findOrCreateDU(ir, node.getContext());
    }

    @Override
    public Iterator<CallSiteReference> iterateCallSites(CGNode node) {
        MethodModel sm = (MethodModel) node.getMethod();
        SSAInstruction[] statements = sm.getStatements(node.getContext());
        List<CallSiteReference> result = new LinkedList<>();
        SSAInstruction.Visitor v =
                new SSAInstruction.Visitor() {
                    @Override
                    public void visitInvoke(SSAInvokeInstruction instruction) {
                        result.add(instruction.getCallSite());
                    }
                };
        for (SSAInstruction s : statements) {
            if (s != null) {
                s.visit(v);
            }
        }
        return result.iterator();
    }
}