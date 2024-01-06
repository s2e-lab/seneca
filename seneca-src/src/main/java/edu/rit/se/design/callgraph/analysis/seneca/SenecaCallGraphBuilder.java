package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DelegatingContextSelector;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.*;
import com.ibm.wala.ipa.callgraph.propagation.rta.RTAContextInterpreter;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.CancelRuntimeException;
import edu.rit.se.design.callgraph.analysis.AbstractSerializationCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.SerializationPointsToSolver;
import edu.rit.se.design.callgraph.dispatcher.IDispatcher;
import edu.rit.se.design.callgraph.model.MethodModel;
import edu.rit.se.design.callgraph.util.SerializationUtils;
import edu.rit.se.design.callgraph.util.TypeCategory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.ibm.wala.types.ClassLoaderReference.Primordial;
import static com.ibm.wala.types.TypeReference.JavaUtilHashSet;
import static edu.rit.se.design.callgraph.analysis.seneca.TaintedSerializationHandler.SearchConfig.*;
import static edu.rit.se.design.callgraph.util.NameUtils.*;
import static edu.rit.se.design.callgraph.util.SerializationUtils.JavaUtilArrayList;
import static edu.rit.se.design.callgraph.util.SerializationUtils.JavaUtilHashMap;
import static edu.rit.se.design.dodo.utils.wala.WalaUtils.isApplicationScope;
import static edu.rit.se.design.dodo.utils.wala.WalaUtils.isExtensionScope;

/**
 * Abstract class that implements the taint propagation rules.
 *
 * @author Reese A. Jones
 * @author Joanna C. S. Santos
 */
public abstract class SenecaCallGraphBuilder extends AbstractSerializationCallGraphBuilder implements ITaintedCallGraphBuilder {

    /**
     * Computes the set of possible types based on a method invocation
     */
    private final IDispatcher dispatcher;
    /**
     * Set of tainted nodes to check against
     */
    protected Set<CGNode> taintedNodes;
    /**
     * What variables in the SSA are tainted
     */
    protected Set<PointerKey> taintedPointers;
    /**
     * The tainted serialization handler (deal with the actual processing of the worklists)
     */
    protected TaintedSerializationHandler taintedSerializationHandler;


    /**
     * @param abstractRootMethod
     * @param options
     * @param cache
     * @param pointerKeyFactory
     * @param appContextSelector
     * @param delegatedAppContextSelector
     * @param appContextInterpreter
     * @param delegatedAppContextInterpreter
     * @param defaultPaPolicy
     * @param taintedPaPolicy
     */
    protected SenecaCallGraphBuilder(
            IMethod abstractRootMethod,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            PointerKeyFactory pointerKeyFactory,
            ContextSelector appContextSelector,
            ContextSelector delegatedAppContextSelector,
            SSAContextInterpreter appContextInterpreter,
            SSAContextInterpreter delegatedAppContextInterpreter,
            PointerAnalysisPolicy defaultPaPolicy,
            PointerAnalysisPolicy taintedPaPolicy,
            IDispatcher dispatcher) {
        super(abstractRootMethod, options, cache, pointerKeyFactory, defaultPaPolicy, taintedPaPolicy);


        this.taintedNodes = new HashSet<>();
        this.taintedPointers = new HashSet<>();
        this.dispatcher = dispatcher;


        if (options == null) throw new IllegalArgumentException("options parameter cannot be null");

        // Since ZeroX has some instance key weirdness, makeSSAContextInterpreter needs to be called before makeContextSelector
        SSAContextInterpreter ctxInterpreter = makeSSAContextInterpreter(appContextInterpreter, delegatedAppContextInterpreter);
        setContextInterpreter(ctxInterpreter);

        // Since nCFA has some instance key weirdness, makeInstanceKeyFactory needs to be called after setContextInterpreter
        InstanceKeyFactory ikFactory = makeInstanceKeyFactory();
        setInstanceKeys(ikFactory);

        // Since ZeroXContainer has some instance key weirdness, makeContextSelector needs to be called after makeInstanceKeyFactory
        ContextSelector ctxSelector = makeContextSelector(appContextSelector, delegatedAppContextSelector);
        setContextSelector(ctxSelector);
    }

    /**
     * Computes whether a given receiver object should be instantiated or not.
     * It will be instantiated if the receiver matches any of the following conditions:
     * (C1): "this" pointer in the inner consumer for a lambda call
     * (C2): call is non-static, receiver object is not the "this" pointer and it is not a call to a constructor
     *
     * @param instruction an invocation instruction whose receiver object needs to be verified
     * @return true whether the receiver object of the invocation needs to be instantiated, false otherwise
     */
    private static boolean shouldInstantiate(CGNode node, SSAAbstractInvokeInstruction instruction) {
        // (C1) it is the "this" object in an inner consumer for a lambda call
        if (!instruction.isStatic() && node.getMethod().getName().toString().startsWith("lambda$")) return true;
        // (C2): (a) non-static; (b) receiver obj is not "this"; (c) not a call to a constructor (<init>);
        return (!instruction.isStatic() && instruction.getReceiver() != 1 && !instruction.getDeclaredTarget().isInit());
    }


    //<editor-fold desc="Making Delegating Objects">

    @Override
    protected IPointsToSolver makeSolver() {
        this.taintedSerializationHandler = new TaintedSerializationHandler(this);
        IPointsToSolver delegateSolver = super.makeSolver();
        return new SerializationPointsToSolver(system, this, taintedSerializationHandler, delegateSolver);
    }

    /**
     * Makes the ContextSelector to be used with tainted and untainted nodes
     *
     * @param appContextSelector          The inputed context selector, can be null or another ContextSelector
     * @param delegatedAppContextSelector The inputed context selector, can be null or another ContextSelector
     * @return The context selector to use for a taintable graph
     */
    protected ContextSelector makeContextSelector(ContextSelector appContextSelector, ContextSelector delegatedAppContextSelector) {
        ContextSelector primaryCtxSelector = makeDefaultContextSelector(appContextSelector);
        ContextSelector taintedCtxSelector = makeTaintedContextSelector(null);
        return new SenecaContextSelector(this, primaryCtxSelector, taintedCtxSelector);
    }

    /**
     * Makes the ContextSelector to be used with tainted and untainted nodes
     *
     * @param appContextInterpreter
     * @param delegatedAppContextInterpreter
     * @return
     */
    protected SSAContextInterpreter makeSSAContextInterpreter(SSAContextInterpreter appContextInterpreter, SSAContextInterpreter delegatedAppContextInterpreter) {
        return new SenecaContextInterpreter(
                this,
                makeDefaultContextInterpreter(appContextInterpreter),
                makeTaintedContextInterpreter(delegatedAppContextInterpreter));
    }
    //</editor-fold>

    //<editor-fold desc="Making Primary (untainted) Objects">

    /**
     * Makes the InstanceKeyFactory to be used with tainted and untainted nodes
     *
     * @return
     */
    protected InstanceKeyFactory makeInstanceKeyFactory() {
        return new SenecaInstanceKeyFactory(
                this,
                makeDefaultMainInstanceKeyFactory(),
                makeTaintedInstanceKeyFactory());
    }

    /**
     * Makes the ContextSelector to be used in an untainted node
     *
     * @param appContextSelector The context selector to use in the primary
     * @return The ContextSelector for the specific class
     */
    protected abstract ContextSelector makeDefaultContextSelector(ContextSelector appContextSelector);

    /**
     * Makes the SSAContextInterpreter to be used in an untainted node
     *
     * @param appContextSelector The context interpreter to use in the primary
     * @return The SSAContextInterpreter for the specific class
     */
    protected abstract SSAContextInterpreter makeDefaultContextInterpreter(SSAContextInterpreter appContextSelector);
    //</editor-fold>

    //<editor-fold desc="Making Secondary (tainted) Objects">

    /**
     * Makes the InstanceKeyFactory to be used in an untainted node
     *
     * @return The InstanceKeyFactory for the specific class
     */
    protected abstract InstanceKeyFactory makeDefaultMainInstanceKeyFactory();

    /**
     * Makes the ContextSelector to be used for a taintedNode
     *
     * @param appContextSelector The inputed context selector, can be null or another ContextSelector
     * @return The context selector to use for tainted nodes
     */
    protected ContextSelector makeTaintedContextSelector(ContextSelector appContextSelector) {
        switch (taintedPaPolicy.policyType) {
            case nCFA:
                return makeNCFAContextSelector(appContextSelector, taintedPaPolicy.policyNumber);
            case ZeroXCFA:
                return makeZeroXContextSelector(appContextSelector);
            case ZeroXContainerCFA:
                return makeZeroXContainerContextSelector(appContextSelector, false);
            default:
                // If no proper type is found, use the same as the specific
                return makeDefaultContextSelector(appContextSelector);
        }
    }

    /**
     * Makes the SSAContextInterpreter to be used for a taintedNode
     *
     * @param appSSAContextInterpreter The inputed context selector, can be null or another ContextSelector
     * @return The context interpreter to use for tainted nodes
     */
    protected SSAContextInterpreter makeTaintedContextInterpreter(SSAContextInterpreter appSSAContextInterpreter) {
        switch (taintedPaPolicy.policyType) {
            case nCFA:
                return makeNCFAContextInterpreter(appSSAContextInterpreter);
            case ZeroXCFA:
                return makeZeroXContextInterpreter(appSSAContextInterpreter);
            case ZeroXContainerCFA:
            default:
                // If no proper type is found, use the same as the specific
                return makeDefaultContextInterpreter(appSSAContextInterpreter);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Making n-CFA Objects">

    /**
     * Makes the InstanceKeyFactory to be used for a taintedNode
     *
     * @return The InstanceKeyFactory to use for tainted nodes
     */
    protected InstanceKeyFactory makeTaintedInstanceKeyFactory() {
        switch (taintedPaPolicy.policyType) {
            case nCFA:
                return makeNCFAInstanceKeyFactory();
            case ZeroXCFA:
                return makeZeroXInstanceKeyFactory(taintedPaPolicy.policyNumber);
            case ZeroXContainerCFA:
            default:
                // If no proper type is found, use the same as the specific
                return makeDefaultMainInstanceKeyFactory();
        }
    }

    /**
     * Makes the same context selector as nCFABuilder
     *
     * @param appContextSelector a custom selector that the client application can provide
     * @param val                a value for the n in nCFA
     * @return The ContextSelector for nCFA
     */
    protected ContextSelector makeNCFAContextSelector(ContextSelector appContextSelector, int val) {
        ContextSelector def = new DefaultContextSelector(options, cha);
        ContextSelector contextSelector =
                appContextSelector == null ? def : new DelegatingContextSelector(appContextSelector, def);
        return new nCFAContextSelector(val, contextSelector);
    }

    /**
     * Makes the same context interpreter as nCFABuilder
     *
     * @param appContextInterpreter a custom interpreter that the client application can provide
     * @return The SSAContextInterpreter for nCFA
     */
    protected SSAContextInterpreter makeNCFAContextInterpreter(SSAContextInterpreter appContextInterpreter) {
        SSAContextInterpreter defI = new DefaultSSAInterpreter(options, cache);
        defI =
                new DelegatingSSAContextInterpreter(
                        ReflectionContextInterpreter.createReflectionContextInterpreter(
                                cha, options, getAnalysisCache()),
                        defI);
        SSAContextInterpreter contextInterpreter =
                appContextInterpreter == null
                        ? defI
                        : new DelegatingSSAContextInterpreter(appContextInterpreter, defI);
        return contextInterpreter;
    }
    //</editor-fold>

    //<editor-fold desc="Making ZeroX Objects">

    /**
     * Makes the same InstanceKeyFactory as nCFABuilder
     *
     * @return The InstanceKeyFactory for nCFA
     */
    protected InstanceKeyFactory makeNCFAInstanceKeyFactory() {
        return new ClassBasedInstanceKeys(options, cha);
    }

    /**
     * Makes the same context selector as ZeroXBuilder
     *
     * @param appContextSelector
     * @return The ContextSelector for ZeroX
     */
    protected ContextSelector makeZeroXContextSelector(ContextSelector appContextSelector) {
        ContextSelector def = new DefaultContextSelector(options, cha);
        ContextSelector contextSelector =
                appContextSelector == null ? def : new DelegatingContextSelector(appContextSelector, def);
        return contextSelector;
    }

    /**
     * Makes the same context interpreter as ZeroXBuilder
     *
     * @param appContextInterpreter
     * @return The SSAContextInterpreter for ZeroX
     */
    protected SSAContextInterpreter makeZeroXContextInterpreter(SSAContextInterpreter appContextInterpreter) {
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

    //</editor-fold>

    //<editor-fold desc="Making ZeroXContainer Object">

    /**
     * Makes the same InstanceKeyFactory as ZeroXBuilder
     *
     * @param val the X in ZeroX
     * @return The InstanceKeyFactory for ZeroX
     */
    protected InstanceKeyFactory makeZeroXInstanceKeyFactory(int val) {
        RTAContextInterpreter contextInterpreter = getContextInterpreter();
        if (contextInterpreter == null)
            throw new RuntimeException("ContextInterpreter was null, while creating a ZeroXInstanceKeyFacotry");

        return new ZeroXInstanceKeys(options, cha, contextInterpreter, val);
    }

    /**
     * Makes the same context selector as ZeroXContainerBuilder
     *
     * @param appContextSelector
     * @param primaryInstanceKeyFactory Whether the primary instance key factory is the one to get from the delegatingInstanceKeyFactory
     * @return The ContextSelector for ZeroXContainer
     */
    protected ContextSelector makeZeroXContainerContextSelector(ContextSelector appContextSelector, boolean primaryInstanceKeyFactory) {

        SenecaInstanceKeyFactory senecaInstanceKeyFactory = (SenecaInstanceKeyFactory) getInstanceKeys();
        if (senecaInstanceKeyFactory == null)
            throw new RuntimeException("InstanceKeyFactory was null while making ZeroXContainerContextSelector");

        ZeroXInstanceKeys zeroXInstanceKeys;
        if (primaryInstanceKeyFactory)
            zeroXInstanceKeys = (ZeroXInstanceKeys) senecaInstanceKeyFactory.getPrimaryInstanceKeyFactory();
        else
            zeroXInstanceKeys = (ZeroXInstanceKeys) senecaInstanceKeyFactory.getSecondaryInstanceKeyFactory();

        if (zeroXInstanceKeys == null)
            throw new RuntimeException("InstanceKeyFactory delegate was null while making ZeroXContainerContextSelector");

        return new DelegatingContextSelector(
                new ContainerContextSelector(cha, zeroXInstanceKeys),
                makeZeroXContextSelector(appContextSelector));
    }
    //</editor-fold>

    //<editor-fold desc="Helper Function(s)">


    /**
     * Obtains the computed tainted pointers.
     *
     * @return the set of tainted pointers.
     */
    public Set<PointerKey> getTaintedPointers() {
        return taintedPointers;
    }

    /**
     * Obtains call graphs
     *
     * @return
     */
    public Set<CGNode> getTaintedNodes() {
        return taintedNodes;
    }


    @Override
    public boolean isTainted(CGNode node) {
        return taintedNodes.contains(node);
    }

    public void setTainted(CGNode node) {
        taintedNodes.add(node);
    }

    public boolean isTainted(PointerKey pk) {
        return taintedPointers.contains(pk);
    }

    public void setTainted(PointerKey pk) {
        taintedPointers.add(pk);
    }


    /**
     * Taints all the fields of an object, whose allocation is represented by an InstanceKey.
     *
     * @param objectInstance
     */
    private void taintAllFields(InstanceKey objectInstance) {
        for (IField field : objectInstance.getConcreteType().getAllInstanceFields()) {
            PointerKey pointerKeyForInstanceField = getPointerKeyForInstanceField(objectInstance, field);
            setTainted(pointerKeyForInstanceField);
        }
    }


    private boolean hasReturnValue(TypeReference returnType) {
        return !returnType.equals(TypeReference.Void) &&
                !returnType.equals(TypeReference.findOrCreate(Primordial, TypeName.string2TypeName("Ljava/lang/Void")));
    }

    //</editor-fold>

    //<editor-fold desc="Visitor Rules">

    @Override
    protected boolean isNodeInRightScope(CGNode n) {
        TaintedSerializationHandler.SearchConfig searchConfig = taintedSerializationHandler.getSearchConfig();
        // (C1) all nodes
        return (searchConfig == ALL) ||
                // (C2) application-only nodes
                (searchConfig == APPLICATION_ONLY && isApplicationScope(n)) ||
                // (C3) application- or extension-level nodes
                (searchConfig == APP_AND_LIBRARIES && (isApplicationScope(n) || isExtensionScope(n)));
    }


    @Override
    protected boolean shouldModel(CGNode caller, CallSiteReference site, CGNode target) {
        IMethod targetMethod = target.getMethod();
        TypeReference typeRef = targetMethod.getDeclaringClass().getReference();
        Selector targetSelector = targetMethod.getSelector();
        if (magicMethods.contains(caller) && targetSelector.equals(readObjectSelector)) {
            /* [T-INVOKE-RETURN-READ-OBJECT]: taints the return value of v = ObjectInputStream.readObject()*/
            /* that is, marks T(v) = tainted */
            for (SSAAbstractInvokeInstruction call : caller.getIR().getCalls(site)) {
                PointerKey pkReceiverReturnValue = getPointerKeyForLocal(caller, call.getDef());
                setTainted(pkReceiverReturnValue);
            }

            return false;
        }
        return true;
    }

    @Override
    protected Set<CGNode> getTargetsForCall(CGNode caller, SSAAbstractInvokeInstruction instruction, InstanceKey[][] invs) {
        if (DEBUG_SENECA_VISITOR) System.out.println("\tgetTargetsForCall: " + instruction);
        return super.getTargetsForCall(caller, instruction, invs);
    }


    @Override
    protected void processCallingConstraints(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target, InstanceKey[][] constParams, PointerKey uniqueCatchKey) {
        if (DEBUG_SENECA_VISITOR)
            System.out.println("\tprocessCallingConstraints " + caller.getMethod().getSignature() + " -> " + target.getMethod().getSignature());

        IMethod callerMethod = caller.getMethod();

        boolean isMagicMethodEntrypoint = isMagicMethodEntrypoint(target.getMethod());
        if (isMagicMethodEntrypoint && (callerMethod instanceof MethodModel || callerMethod instanceof FakeRootMethod)) {
            // it is calling a magic method (either from a serialization point or as an entrypoint)
            introduceTaint(caller, instruction, target);

            // if it is the readObject callback method, sets the ois parameters as tainted
            if (target.getMethod().getSelector().equals(readObjectCallbackSelector)) {
                setTainted(getPointerKeyForLocal(target, 2));
            }

        } else {
            // for all the other cases (non-entrypoint methods)
            propagateTInvokeArgs(caller, instruction, target, constParams);
        }


        super.processCallingConstraints(caller, instruction, target, constParams, uniqueCatchKey);
    }

    /**
     * Implements the rules:
     * <ul>
     *      <li>[T-INTRODUCTION-CALLSITE] taints the gadget instantiation</li>
     *      <li>[T-INTRODUCTION-FIELDS] taints all fields from gadget objects</li>
     *      <li>[T-INTRODUCTION-CALLEE] taints the "this" pointer of the gadget class' magic method</li>
     * </ul>
     *
     * @param caller      the calling entrypoint
     * @param instruction invocation of the gadget class' constructor
     * @param target
     */
    private void introduceTaint(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target) {

        PointerKey pointerKeyGadgetClass = getPointerKeyForLocal(caller, instruction.getReceiver());
        // TODO check this if condition on enforcing points-to-sets equals to 1
//        if (getPointerAnalysis().getPointsToSet(pointerKeyGadgetClass).size() != 1)
//            throw new IllegalStateException("Points to set size should be equals to 1 " + instruction+ " it is " + getPointerAnalysis().getPointsToSet(pointerKeyGadgetClass).size());

        /* [T-INTRODUCTION-CALLSITE]: mark gadget instantiation as tainted */
        setTainted(pointerKeyGadgetClass);

        /* [T-INTRODUCTION-CALLEE]: mark the "this" object in the next callgraph node */
        PointerKey pointerKeyForThis = getPointerKeyForLocal(target, 1);
        setTainted(pointerKeyForThis);

        /* [T-INTRODUCTION-FIELDS] */
        // we iterate over all instances of the class
        Iterator<InstanceKey> iterator = getPointerAnalysis().getPointsToSet(pointerKeyGadgetClass).iterator();
        // mark its (non-static) fields
        iterator.forEachRemaining(gadgetInstance -> {
            taintAllFields(gadgetInstance);
        });
//        InstanceKey gadgetInstance = iterator.next(); // there should be only one allocation per magic method
//        taintAllFields(gadgetInstance);
//        assert iterator.hasNext() == false; // enforce assumption of one allocation only
    }

    /**
     * Handles the instructions:
     * <ul>
     *     <li>x = o.g(a1,a2,...,an)</li>
     *     <li>x = T.g(a1,a2,...,an)</li>
     * </ul>
     *
     * @param caller      call graph node that makes the (static/instance) invocation
     * @param instruction
     * @param target
     * @param constParams
     */
    private void propagateTInvokeArgs(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target, InstanceKey[][] constParams) {
        /* [T-INVOKE-ARGS-INSTANCE]: propagates the taint state of arguments passed to an invoked object method and its this parameter */
        /* [T-INVOKE-ARGS-STATIC]: propagates the taint state of arguments passed to an invoked static method */
        for (int i = 0; i < instruction.getNumberOfPositionalParameters(); i++) {
            // we don't need to separately handle static and non-static calls because the this pointer is implicitly represented as arg1 in instance methods
            if (target.getMethod().getParameterType(i).isReferenceType()) {
                if (constParams == null || constParams[i] == null) {
                    PointerKey pkForReceivedParameter = getTargetPointerKey(target, i);
                    PointerKey pkForPassedArgument = getPointerKeyForLocal(caller, instruction.getUse(i));
                    if (isTainted(pkForPassedArgument))
                        setTainted(pkForReceivedParameter);
                }
            }
        }
    }

    /**
     * Handles method invocations: both instance and static ones:
     * x = o.g(a1,a2,…,an)
     * x = T.g(a1,a2,…,an)
     * Rules:
     * <ul>
     *      <li>[T-INVOKE-RETURN-INSTANCE]: taints the variable that receives the result if the returned object is tainted</li>
     *      <li>[T-INVOKE-RETURN-STATIC]: taints the variable that receives the result if the returned object is tainted</li>
     * </ul>
     *
     * @param instruction
     */
    private void propagateTInvoke(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target) {
        TypeReference returnType = instruction.getCallSite().getDeclaredTarget().getReturnType();

        if (hasReturnValue(returnType)) {
            PointerKey pkReceiverReturnValue = getPointerKeyForLocal(caller, instruction.getDef());
            /* [T-INVOKE-RETURN-INSTANCE], [T-INVOKE-RETURN-STATIC]: taints the return value of a method (if any) if the actual returned value by the target is tainted (precise implementation)*/
            PointerKey pointerKeyForReturnValue = getPointerKeyForReturnValue(target);
            if (isTainted(pointerKeyForReturnValue))
                setTainted(pkReceiverReturnValue);
        }
    }


    @Override
    protected ConstraintVisitor makeVisitor(CGNode node) {
        return new TaintedVisitor(this, node);
    }

    public class TaintedVisitor extends ConstraintVisitor {

        public TaintedVisitor(SSAPropagationCallGraphBuilder builder, CGNode node) {
            super(builder, node);
        }

        // Does not spread taint, can be removed
        public void visitGoto(SSAGotoInstruction instruction) {
            super.visitGoto(instruction);
        }

        @Override
        public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitArrayLoad " + instruction);
            propagateTArrayLoad(instruction);
            super.visitArrayLoad(instruction);
        }

        @Override
        public void visitArrayStore(SSAArrayStoreInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitArrayStore " + instruction);
            propagateTArrayStore(instruction);
            super.visitArrayStore(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitBinaryOp(SSABinaryOpInstruction instruction) {
            super.visitBinaryOp(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
            super.visitUnaryOp(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitConversion(SSAConversionInstruction instruction) {
            super.visitConversion(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitComparison(SSAComparisonInstruction instruction) {
            super.visitComparison(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
            super.visitConditionalBranch(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitSwitch(SSASwitchInstruction instruction) {
            super.visitSwitch(instruction);
        }

        @Override
        public void visitReturn(SSAReturnInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitReturn " + instruction);
            propagateTReturn(instruction);
            super.visitReturn(instruction);
        }

        @Override
        public void visitGet(SSAGetInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitGet " + instruction);
            propagateTLoad(instruction);
            super.visitGet(instruction);
        }

        @Override
        public void visitPut(SSAPutInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitPut " + instruction);
            propagateTStore(instruction);
            super.visitPut(instruction);
        }

        @Override
        public void visitInvoke(SSAInvokeInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitInvoke " + instruction);
            // this only makes sense if the node is not the fake root synthetic method
            if (!(node.getMethod() instanceof FakeRootMethod) /*&& !entrypointCallSites.contains(instruction.getCallSite())*/) {
                if (shouldInstantiate(node, instruction))
                    computeTInvokeSideEffect(node, instruction);
                if (!instruction.isStatic() && isTainted(getPointerKeyForLocal(instruction.getReceiver()))) {
                    setTainted(node);
                }

                Set<CGNode> possibleTargets = getCallGraph().getPossibleTargets(node, instruction.getCallSite());
                for (CGNode possibleTarget : possibleTargets) {
                    propagateTInvoke(node, instruction, possibleTarget);
                }
            }
            super.visitInvoke(instruction);
        }


        @Override
        // Spreads/causes taint depending on implementation
        public void visitNew(SSANewInstruction instruction) {
            super.visitNew(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitArrayLength(SSAArrayLengthInstruction instruction) {
            super.visitArrayLength(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitThrow(SSAThrowInstruction instruction) {
            super.visitThrow(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitMonitor(SSAMonitorInstruction instruction) {
            super.visitMonitor(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitCheckCast(SSACheckCastInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitCheckCast " + instruction);
            propagateTCheckCast(instruction);
            super.visitCheckCast(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitInstanceof(SSAInstanceofInstruction instruction) {
            super.visitInstanceof(instruction);
        }

        @Override
        public void visitPhi(SSAPhiInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitPhi " + instruction);
            propagateTPhi(instruction);
            super.visitPhi(instruction);
        }

        @Override
        // This can cause taints, but callgraphs can be constructed without Pi
        public void visitPi(SSAPiInstruction instruction) {
            if (DEBUG_SENECA_VISITOR) System.out.println(node + "\n\tvisitPi " + instruction);
            PointerKey def = getPointerKeyForLocal(instruction.getDef());
            if (!isTainted(def) && isTainted(getPointerKeyForLocal(instruction.getUse(0))))
                setTainted(def);
            super.visitPi(instruction);
        }

        @Override
        // Does not spread taint, can be removed
        public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
            super.visitGetCaughtException(instruction);
        }

        @Override
        // Spreads taint depending on implementation
        public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
            super.visitLoadMetadata(instruction);
        }

        //</editor-fold>

        //<editor-fold desc="Taint Propagation Rules">


        /**
         * Handles instructions like:
         * x = y.f (instance field)
         * x = T.f (static field)
         *
         * @param instruction
         */
        private void propagateTLoad(SSAGetInstruction instruction) {

            IField field = cha.resolveField(instruction.getDeclaredField());
            // sometimes fields are null because it belongs to an excluded class listed on the exclusion file
            // nothing can be inferred
            if (field != null) {
                //the local variable number for y  in the instruction x = y.f
                int ref = instruction.getRef();
                // pointer for left hand side (LHS) operator
                PointerKey pointerKeyLhs = getPointerKeyForLocal(instruction.getDef()); // x in x = y.f

                /* [T-LOAD-STATIC]: taints a variable that access a tainted static field */
                if (instruction.isStatic()) {
                    PointerKey pointerKeyForStaticRef = getPointerKeyForStaticField(field);
                    if (isTainted(pointerKeyForStaticRef))
                        setTainted(pointerKeyLhs);
                } else {
                    /* [T-LOAD-INSTANCE]: taints a variable that access a tainted instance field */
                    PointerKey pointerKeyForRef = getPointerKeyForLocal(ref); // y in x = x.y
                    // (A): y is tainted itself;
                    if (isTainted(pointerKeyForRef))
                        setTainted(pointerKeyLhs);

                    // (B): field y.f itself is tainted for y
                    // notice that this will be as precise as the pointer analysis for the variable y
                    for (InstanceKey instanceKey : builder.getPointerAnalysis().getPointsToSet(pointerKeyForRef)) {
                        PointerKey pointerKeyForInstanceField = builder.getPointerKeyForInstanceField(instanceKey, field);
                        if (isTainted(pointerKeyForInstanceField))
                            setTainted(pointerKeyLhs);
                    }
                }
            }
        }

        /**
         * Implements rules for instructions like: x.f = y
         *
         * @param instruction
         */
        private void propagateTStore(SSAPutInstruction instruction) {
            IField iField = cha.resolveField(instruction.getDeclaredField());
            PointerKey pkForRhs = getPointerKeyForLocal(instruction.getVal());
            if (iField != null && taintedPointers.contains(pkForRhs)) {
                if (instruction.isStatic()) {
                    /* [T-STORE-STATIC]: taints a static field that accesses a tainted variable */
                    PointerKey pkForStaticField = getPointerKeyForStaticField(iField);
                    taintedPointers.add(pkForStaticField);
                } else {
                    /* [T-STORE-INSTANCE]: taints an instance field that accesses a tainted variable */
                    PointerKey pkReferenceObj = getPointerKeyForLocal(instruction.getRef());

                    // taints only the field for all instantiated objects that contains it
                    for (InstanceKey instanceKey : getPointerAnalysis().getPointsToSet(pkReferenceObj)) {
                        PointerKey pointerKeyForInstanceField = getPointerKeyForInstanceField(instanceKey, iField);
                        taintedPointers.add(pointerKeyForInstanceField);
                    }
                }
            }
        }


        /**
         * Handles the instruction: <code>return x</code>
         *
         * @param instruction the return instruction in SSA
         */
        private void propagateTReturn(SSAReturnInstruction instruction) {
            /* [T-RETURN]: taints the mock return value of m */
            if (!instruction.returnsVoid()) {
                PointerKey pkForReturnLocal = getPointerKeyForLocal(instruction.getResult());
                PointerKey pointerKeyForReturnValue = getPointerKeyForReturnValue();
                if (isTainted(pkForReturnLocal) && !isTainted(pointerKeyForReturnValue)) {
                    setTainted(pointerKeyForReturnValue);
                    computeTReturnSideEffect(instruction);
                }
            }
        }


        /**
         * Handles the instruction:  x = y[i]
         *
         * @param instruction
         */
        private void propagateTArrayLoad(SSAArrayLoadInstruction instruction) {
            /* [T-ARRAY-LOAD]: taints x if the array y is tainted*/
            PointerKey y = getPointerKeyForLocal(instruction.getArrayRef());
            if (isTainted(y)) {
                PointerKey x = getPointerKeyForLocal(instruction.getDef());
                setTainted(x);
            }
        }


        /**
         * Handles the instruction:  x[i] = y
         *
         * @param instruction
         */
        private void propagateTArrayStore(SSAArrayStoreInstruction instruction) {
            /* [T-ARRAY-STORE]: taints all the elements within an array x if a tainted value is added to it (container-insensitive rule) */
            PointerKey y = getPointerKeyForLocal(instruction.getValue());
            if (isTainted(y)) {
                PointerKey x = getPointerKeyForLocal(instruction.getArrayRef());
                setTainted(x);
            }
        }

        /**
         * Handles the instruction:  Φ = v1, v2, ..., vN
         *
         * @param instruction
         */
        private void propagateTPhi(SSAPhiInstruction instruction) {
            PointerKey phi = getPointerKeyForLocal(instruction.getDef());
            /* [T-PHI]: taints the phi variable if any of the choices are also tainted */
            for (int i = 0; i < instruction.getNumberOfUses(); i++) {
                PointerKey vn = getPointerKeyForLocal(instruction.getUse(i));
                if (isTainted(vn)) {
                    setTainted(phi);
                    return;
                }
            }
        }


        /**
         * Handles the instruction:  x = (T) y
         *
         * @param instruction
         */
        private void propagateTCheckCast(SSACheckCastInstruction instruction) {
            PointerKey castedVariable = getPointerKeyForLocal(instruction.getVal());
            /* [T-CHECKCAST]: taints the variable if any of the choices are also tainted */
            PointerKey downcastResult = getPointerKeyForLocal(instruction.getResult());
            if (isTainted(castedVariable)) {
                setTainted(downcastResult);
            }
        }

        /**
         * Computes the side-effect of adding fake instances to the points to set of a tainted objects.
         *
         * @param node
         * @param instruction
         */
        private void computeTInvokeSideEffect(CGNode node, SSAInvokeInstruction instruction) {
            if (node.getMethod() instanceof MethodModel) return;
            PointerKey pkReceiverObj = getPointerKeyForLocal(instruction.getReceiver());
            if (isTainted(pkReceiverObj)) {
                TypeReference declaringClass = instruction.getDeclaredTarget().getDeclaringClass();
                TypeCategory typeCategory = SerializationUtils.getTypeCategory(cha, declaringClass);

                switch (typeCategory) {
                    // simple objects
                    case OBJECT:
                        Set<IClass> possibleTypes = dispatcher.computePossibleTypesForCall(node, instruction);
                        for (IClass possibleType : possibleTypes) {
//                            FakeInstanceKey taintedInstanceKey = new FakeInstanceKey(possibleType);
                            TaintedInstanceKey taintedInstanceKey = new TaintedInstanceKey(possibleType, node);
                            getSystem().newConstraint(pkReceiverObj, taintedInstanceKey);
                            taintAllFields(taintedInstanceKey);
                        }
                        break;
                    /** Special handling for collections */
                    case LIST:
                        IClass iClassArrayList = cha.lookupClass(JavaUtilArrayList);
                        assert iClassArrayList != null; // enforces that java.util.ArrayList was not excluded
//                        FakeInstanceKey ikArrayList = new FakeInstanceKey(iClassArrayList);
                        TaintedInstanceKey ikArrayList = new TaintedInstanceKey(iClassArrayList, node);
                        getSystem().newConstraint(pkReceiverObj, ikArrayList);
                        taintAllFields(ikArrayList);
                        break;
                    case SET:
                        IClass iClassHashSet = cha.lookupClass(JavaUtilHashSet);
                        assert iClassHashSet != null; // enforces that java.util.HashSet was not excluded
//                        FakeInstanceKey ikHashSet = new FakeInstanceKey(iClassHashSet);
                        TaintedInstanceKey ikHashSet = new TaintedInstanceKey(iClassHashSet, node);
                        getSystem().newConstraint(pkReceiverObj, ikHashSet);
                        taintAllFields(ikHashSet);
                        break;
                    case MAP:
                        IClass iClassHashMap = cha.lookupClass(JavaUtilHashMap);
                        assert iClassHashMap != null;
//                        FakeInstanceKey ikHashMap = new FakeInstanceKey(iClassHashMap);
                        TaintedInstanceKey ikHashMap = new TaintedInstanceKey(iClassHashMap, node);
                        getSystem().newConstraint(pkReceiverObj, ikHashMap);
                        taintAllFields(ikHashMap);
                        break;
                }

            }
        }

        /**
         * Computes the side-effect of re-adding the callers of the method back to the WALA's worklist.
         * This will make the Data Solver go over again the node ([RETURN-SIDE-EFFECT]).
         *
         * @param instruction
         */
        private void computeTReturnSideEffect(SSAReturnInstruction instruction) {
            try {
                Iterator<CGNode> callers = callGraph.getPredNodes(node);
                while (callers.hasNext()) {
                    CGNode caller = callers.next();
                    //attempt to restrain the taint analysis for the deserialization scope
                    if (!(caller instanceof SyntheticMethod))
                        addConstraintsFromChangedNode(caller, monitor);
                }
            } catch (CancelException e) {
                throw new CancelRuntimeException(e);
            }
        }
    }

    //</editor-fold>

}
