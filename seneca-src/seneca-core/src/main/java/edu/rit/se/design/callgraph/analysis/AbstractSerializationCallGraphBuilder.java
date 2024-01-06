package edu.rit.se.design.callgraph.analysis;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallerSiteContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import edu.rit.se.design.callgraph.analysis.seneca.SenecaCallGraphBuilder;
import edu.rit.se.design.callgraph.model.AbstractClassModel;
import edu.rit.se.design.callgraph.model.ObjectInputStreamModel;
import edu.rit.se.design.callgraph.model.ObjectOutputStreamModel;
import edu.rit.se.design.callgraph.util.NameUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashSet;
import java.util.Set;

import static edu.rit.se.design.callgraph.util.NameUtils.*;

public abstract class AbstractSerializationCallGraphBuilder extends SSAPropagationCallGraphBuilder implements ISerializationCallGraphBuilder {

    /**
     * constant for using when getting the "this" pointer in an instruction
     */
    protected static final int THIS_POINTER = 1;
    /**
     * enable for printing statements during call graph construction and visitor
     */
    protected static boolean DEBUG_SALSA_CG = false;
    protected static boolean DEBUG_SALSA_VISITOR = DEBUG_SALSA_CG | false;
    protected static boolean DEBUG_SENECA_VISITOR = DEBUG_SALSA_CG | false;

    /**
     * Set of model nodes (i.e., synthetic methods modeling the Object(In|Out)putStream classes)
     */
    protected final Set<CGNode> models;


    /**
     * Caching results
     */
    protected final IAnalysisCacheView cache;


    /**
     * Worklist for serialization and deserialization: (caller, invoke instruction, target)
     */
    protected final Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> serializationWorkList;
    protected final Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> deserializationWorkList;

    /**
     * The types of the primary(untainted) propagation graph and secondary(tainted) propagation type
     */
    protected final PointerAnalysisPolicy defaultPaPolicy;
    protected final PointerAnalysisPolicy taintedPaPolicy;


    /**
     * Magic methods (both from serialization and deserialization)
     */
    protected Set<CGNode> magicMethods;


    protected AbstractSerializationCallGraphBuilder(IMethod abstractRootMethod,
                                                    AnalysisOptions options,
                                                    IAnalysisCacheView cache,
                                                    PointerKeyFactory pointerKeyFactory,
                                                    PointerAnalysisPolicy defaultPaPolicy,
                                                    PointerAnalysisPolicy taintedPaPolicy) {
        super(abstractRootMethod, options, cache, pointerKeyFactory);
        this.magicMethods = new HashSet<>();
        this.models = new HashSet<>();
        this.cache = cache;
        this.defaultPaPolicy = defaultPaPolicy;
        this.taintedPaPolicy = taintedPaPolicy;
        this.deserializationWorkList = new HashSet<>();
        this.serializationWorkList = new HashSet<>();
    }


    /**
     * @param caller the caller node
     * @param site
     * @param recv
     * @param iKey   an abstraction of the receiver of the call (or null if not applicable)
     * @return the CGNode to which this particular call should dispatch.
     */
    @Override
    protected CGNode getTargetForCall(CGNode caller, CallSiteReference site, IClass recv, InstanceKey[] iKey) {
        if (NameUtils.ALL_MAGIC_METHODS.contains(caller.getMethod().getSelector()))
            magicMethods.add(caller);
        CGNode target = super.getTargetForCall(caller, site, recv, iKey);
        try {
            // verifies whether the resolved target should be replaced by our model
            target = replaceIfNeeded(caller, site, target);
        } catch (CancelException e) {
            target = null;
            e.printStackTrace();
        }
        return target;
    }

    /**
     * Returns true or false indicating whether a {@link edu.rit.se.design.callgraph.model.MethodModel} should be added to the worklist for further refinement
     *
     * @param caller
     * @param site
     * @param target
     * @return
     */
    protected abstract boolean shouldModel(CGNode caller, CallSiteReference site, CGNode target);

    /**
     * Returns true or false indicating whether a {@link CGNode} should be modeled because it is on the correct scope
     * (i.e., application or extension).
     *
     * @param n node to be checked
     * @return true if the node is in the correct scope, false otherwise.
     */
    protected abstract boolean isNodeInRightScope(CGNode n);


    /**
     * Verifies whether the call target should be replaced by our models for {@link java.io.ObjectInputStream} and {@link java.io.ObjectOutputStream} classes.
     * Only replaces for call sites at the application scope.
     *
     * @param caller the caller of the invocation
     * @param target the cg node target of an invocation
     * @return returns the param target as is or a synthetic node to our our models for {@link java.io.ObjectInputStream} and {@link java.io.ObjectOutputStream} classes in case the target has to be replaced.
     */
    protected CGNode replaceIfNeeded(CGNode caller, CallSiteReference site, CGNode target) throws CancelException {
        if (caller == null || target == null) return target;

        IMethod targetMethod = target.getMethod();
        TypeReference typeRef = targetMethod.getDeclaringClass().getReference();
        Selector targetSelector = targetMethod.getSelector();
        if (isNodeInRightScope(caller)) {
            if ((typeRef.equals(JavaIoObjectInputStream) && targetSelector.equals(readObjectSelector)) ||
                    (typeRef.equals(JavaIoObjectOutputStream) && targetSelector.equals(writeObjectSelector))) {
                AbstractClassModel model = typeRef.equals(JavaIoObjectInputStream) ? new ObjectInputStreamModel(cha, options, cache) : new ObjectOutputStreamModel(cha, options, cache);
                Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> worklist = typeRef.equals(JavaIoObjectInputStream) ? deserializationWorkList : serializationWorkList;
                CGNode newTarget = callGraph.findOrCreateNode(model.getMethod(targetSelector), new CallerSiteContext(caller, site));
                if (shouldModel(caller, site, newTarget)) {
                    for (SSAAbstractInvokeInstruction call : caller.getIR().getCalls(site)) {
                        worklist.add(new ImmutableTriple<>(caller, call, newTarget));
                    }
                }
                if (DEBUG_SALSA_CG) System.out.println("Replacing " + target + " to " + newTarget);

                // adds to the set of model methods
                models.add(newTarget);

                // invalidate previous cache
                callGraph.getAnalysisCache().invalidate(targetMethod, target.getContext());
                // if it is taint-based construction, set node as tainted
                if (this instanceof SenecaCallGraphBuilder)
                    ((SenecaCallGraphBuilder) this).setTainted(newTarget);
                return newTarget;
            } else {
                if (typeRef.equals(JavaIoObjectInputStream) || typeRef.equals(JavaIoObjectOutputStream)) {
                    AbstractClassModel model = typeRef.equals(JavaIoObjectInputStream) ? new ObjectInputStreamModel(cha, options, cache) : new ObjectOutputStreamModel(cha, options, cache);
                    IMethod method = model.getMethod(targetSelector);
                    if (method != null) {
                        CGNode newTarget = callGraph.findOrCreateNode(method, new CallerSiteContext(caller, site));
                        if (DEBUG_SALSA_CG)
                            System.out.println("Replacing non-magic method" + target + " to " + newTarget);
                        return newTarget;
                    }

                }
            }
        }
        return target;
    }


    /**
     * @return serialization synthetic methods in the worklist to be processed
     */
    @Override
    public Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> getSerializationWorkList() {
        return serializationWorkList;
    }

    /**
     * @return deserialization synthetic methods in the worklist to be processed
     */
    @Override
    public Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> getDeserializationWorkList() {
        return deserializationWorkList;
    }

    /**
     * Returns all the magic methods in the call graph (i.e., whose signature is in {@link NameUtils#ALL_MAGIC_METHODS}).
     *
     * @return a set of all magic methods in the built call graph.
     */
    @Override
    public Set<CGNode> getMagicMethods() {
        return magicMethods;
    }

}
