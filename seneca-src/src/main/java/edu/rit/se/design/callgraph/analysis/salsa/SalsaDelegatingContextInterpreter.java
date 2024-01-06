package edu.rit.se.design.callgraph.analysis.salsa;


import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.FieldReference;

import java.util.Iterator;

/**
 * Interprets a method and return information needed for CFA.
 * This uses a delegation pattern. If node is tainted, delegates to the A context interpreter, otherwise, it delegates to B.
 *
 * @author Reese A. Jones (raj8065@rit.edu)
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */

public class SalsaDelegatingContextInterpreter implements SSAContextInterpreter {

    SalsaCallGraphBuilder builder;
    /**
     * reference to the interpreter of the underlying PA policy
     */
    private SSAContextInterpreter normalInterpreter;
    /**
     * interpreter tailored for our model nodes (synthetic)
     */
    private SSAContextInterpreter modelNodesInterpreter;

    /**
     * @param builder               callgraph builder
     * @param normalInterpreter     context interpreter for non-model nodes
     * @param modelNodesInterpreter context interpreter for model nodes
     */
    public SalsaDelegatingContextInterpreter(SalsaCallGraphBuilder builder, SSAContextInterpreter normalInterpreter, SSAContextInterpreter modelNodesInterpreter) {

        if (normalInterpreter == null)
            throw new IllegalArgumentException("normalInterpreter for ContextInterpreter delegation is null");
        if (modelNodesInterpreter == null)
            throw new IllegalArgumentException("modelNodesInterpreter for ContextInterpreter delegation is null");
        if (builder == null)
            throw new IllegalArgumentException("Builder for ContextInterpreter delegation is null");

        this.builder = builder;
        this.normalInterpreter = normalInterpreter;
        this.modelNodesInterpreter = modelNodesInterpreter;
    }

    @Override
    public Iterator<NewSiteReference> iterateNewSites(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.iterateNewSites(node) :
                normalInterpreter.iterateNewSites(node);
    }

    @Override
    public Iterator<FieldReference> iterateFieldsRead(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.iterateFieldsRead(node) :
                normalInterpreter.iterateFieldsRead(node);
    }

    @Override
    public Iterator<FieldReference> iterateFieldsWritten(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.iterateFieldsWritten(node) :
                normalInterpreter.iterateFieldsWritten(node);
    }

    @Override
    public boolean recordFactoryType(CGNode node, IClass klass) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.recordFactoryType(node, klass) :
                normalInterpreter.recordFactoryType(node, klass);
    }

    @Override
    public boolean understands(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.understands(node) :
                normalInterpreter.understands(node);
    }

    @Override
    public Iterator<CallSiteReference> iterateCallSites(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.iterateCallSites(node) :
                normalInterpreter.iterateCallSites(node);
    }

    @Override
    public IR getIR(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.getIR(node) :
                normalInterpreter.getIR(node);
    }

    @Override
    public IRView getIRView(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.getIRView(node) :
                normalInterpreter.getIRView(node);
    }

    @Override
    public DefUse getDU(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.getDU(node) :
                normalInterpreter.getDU(node);
    }

    @Override
    public int getNumberOfStatements(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.getNumberOfStatements(node) :
                normalInterpreter.getNumberOfStatements(node);
    }

    @Override
    public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getCFG(CGNode node) {
        return builder.isSyntheticModel(node) ?
                modelNodesInterpreter.getCFG(node) :
                normalInterpreter.getCFG(node);
    }
}
