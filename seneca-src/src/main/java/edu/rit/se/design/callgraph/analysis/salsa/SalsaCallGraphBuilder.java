package edu.rit.se.design.callgraph.analysis.salsa;

import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DelegatingContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.*;
import com.ibm.wala.ipa.callgraph.propagation.rta.RTAContextInterpreter;
import edu.rit.se.design.callgraph.analysis.AbstractSerializationCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy;
import edu.rit.se.design.callgraph.analysis.SerializationPointsToSolver;
import edu.rit.se.design.dodo.utils.wala.WalaUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * A propagation call graph builder that enhances underlying policy by allowing support of serialization and deserialization.
 *
 * @author Reese A. Jones (raj8065@rit.edu)
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public abstract class SalsaCallGraphBuilder extends AbstractSerializationCallGraphBuilder {

    /**
     * Tracks objects serialized and deserialized
     */
    protected Set<PointerKey> deserializedObjects; // set D in the paper
    protected Set<PointerKey> serializedObjects;    // set S in the paper

    protected SalsaCallGraphBuilder(
            IMethod abstractRootMethod,
            AnalysisOptions options,
            IAnalysisCacheView cache,
            PointerKeyFactory pointerKeyFactory,
            ContextSelector appContextSelector,
            ContextSelector delegatedAppContextSelector,
            SSAContextInterpreter appContextInterpreter,
            SSAContextInterpreter delegatedAppContextInterpreter,
            PointerAnalysisPolicy defaultPaPolicy,
            PointerAnalysisPolicy taintedPaPolicy) {
        super(abstractRootMethod, options, cache, pointerKeyFactory, defaultPaPolicy, taintedPaPolicy);


        this.deserializedObjects = new HashSet<>();
        this.serializedObjects = new HashSet<>();

        // Since ZeroX has some has some instance key weirdness, makeSSAContextInterpreter needs to be called before makeContextSelector
        setContextInterpreter(makeSSAContextInterpreter(appContextInterpreter, delegatedAppContextInterpreter));

        // Since nCFA has some instance key weirdness, makeInstanceKeyFactory needs to be called after setContextInterpreter
        setInstanceKeys(makeInstanceKeyFactory());

        // Since ZeroXContainer has some instance key weirdness, makeContextSelector needs to be called after makeInstanceKeyFactory
        setContextSelector(makeContextSelector(appContextSelector, delegatedAppContextSelector));
    }


//<editor-fold desc="Replacing solvers and other methods needed to handle serialization-related features">


    @Override
    protected boolean isNodeInRightScope(CGNode n) {
        return WalaUtils.isApplicationScope(n);
    }

    @Override
    protected boolean shouldModel(CGNode caller, CallSiteReference site, CGNode target) {
        return true;
    }

    @Override
    protected IPointsToSolver makeSolver() {
        UnsoundSerializationHandler serializationHandler = new UnsoundSerializationHandler(this);
        IPointsToSolver delegateSolver = super.makeSolver();
        return new SerializationPointsToSolver(system, this, serializationHandler, delegateSolver);
    }

//</editor-fold>

//<editor-fold desc="Making Delegating Objects">

    /**
     * Makes the ContextSelector to be used with tainted and untainted nodes
     *
     * @param appContextSelector          The inputted context selector, can be null or another ContextSelector
     * @param delegatedAppContextSelector The inputted context selector, can be null or another ContextSelector
     * @return The context selector to use for a taint-based call graph construction
     */
    protected ContextSelector makeContextSelector(
            ContextSelector appContextSelector,
            ContextSelector delegatedAppContextSelector) {
        return new SalsaDelegatingContextSelector(
                this,
                makePrimaryContextSelector(appContextSelector),
                makeSecondaryContextSelector(delegatedAppContextSelector));
    }

    /**
     * Makes the ContextSelector to be used with normal and model nodes
     *
     * @param appContextInterpreter
     * @param delegatedAppContextInterpreter
     * @return
     */
    protected SSAContextInterpreter makeSSAContextInterpreter(
            SSAContextInterpreter appContextInterpreter,
            SSAContextInterpreter delegatedAppContextInterpreter) {
        SSAContextInterpreter normalInterpreter = makePrimarySSAContextInterpreter(appContextInterpreter);
        SSAContextInterpreter modelInterpreter = makeSecondaryContextInterpreter(delegatedAppContextInterpreter);
        return new SalsaDelegatingContextInterpreter(this, normalInterpreter, modelInterpreter);
    }

    /**
     * Makes the InstanceKeyFactory to be used with normal and model nodes
     *
     * @return
     */
    protected InstanceKeyFactory makeInstanceKeyFactory() {
        return new SalsaDelegatingInstanceKeyFactory(
                this,
                makePrimaryInstanceKeyFactory(),
                makeSecondaryInstanceKeyFactory());
    }
//</editor-fold>

//<editor-fold desc="Making Primary Objects (i.e., according to the underlying PA policy)">

    /**
     * Makes the ContextSelector to be used in a normal node
     *
     * @param appContextSelector The context selector to use in the primary
     * @return The ContextSelector for the specific class
     */
    protected abstract ContextSelector makePrimaryContextSelector(ContextSelector appContextSelector);

    /**
     * Makes the SSAContextInterpreter to be used in a normal node
     *
     * @param appContextSelector The context interpreter to use in the primary
     * @return The SSAContextInterpreter for the specific class
     */
    protected abstract SSAContextInterpreter makePrimarySSAContextInterpreter(SSAContextInterpreter appContextSelector);

    /**
     * Makes the InstanceKeyFactory to be used in a normal node
     *
     * @return The InstanceKeyFactory for the specific class
     */
    protected abstract InstanceKeyFactory makePrimaryInstanceKeyFactory();
//</editor-fold>

//<editor-fold desc="Making Secondary Objects for Model Nodes">

    /**
     * Makes the ContextSelector to be used for a synthetic (model) node.
     *
     * @param appContextSelector The inputted context selector, can be null or another ContextSelector
     * @return The context selector to use for model (synthetic) nodes
     */
    protected ContextSelector makeSecondaryContextSelector(
            ContextSelector appContextSelector) {
        switch (taintedPaPolicy.policyType) {
            case nCFA:
                return makeNCFAContextSelector(appContextSelector, taintedPaPolicy.policyNumber);
            case ZeroXCFA:
                return makeZeroXContextSelector(appContextSelector);
            case ZeroXContainerCFA:
                return makeZeroXContainerContextSelector(appContextSelector, false);
            default:
                throw new UnsupportedOperationException("Unknown policy " + taintedPaPolicy);
        }
    }

    /**
     * Makes the SSAContextInterpreter to be used for a model node
     *
     * @param appSSAContextInterpreter The inputted context selector, can be null or another ContextSelector
     * @return The context interpreter to use for tainted nodes
     */
    protected SSAContextInterpreter makeSecondaryContextInterpreter(
            SSAContextInterpreter appSSAContextInterpreter) {
//        switch (analysisPolicy) {
//            case nCFA:
//                return makeNCFAContextInterpreter(appSSAContextInterpreter);
//            case ZeroXCFA:
//                return makeZeroXContextInterpreter(appSSAContextInterpreter);
//            case ZeroXContainerCFA:
//            default:
//                // If no proper type is found, use the same as the specific
//                return makePrimarySSAContextInterpreter(appSSAContextInterpreter);
//        }
//        return makeNCFAContextInterpreter(appSSAContextInterpreter);
        return new SalsaContextInterpreter(options, cache);
    }

    /**
     * Makes the InstanceKeyFactory to be used for a taintedNode
     *
     * @return The InstanceKeyFactory to use for tainted nodes
     */
    protected InstanceKeyFactory makeSecondaryInstanceKeyFactory() {
        switch (taintedPaPolicy.policyType) {
            case nCFA:
                return makeNCFAInstanceKeyFactory();
            case ZeroXCFA:
                return makeZeroXInstanceKeyFactory(taintedPaPolicy.policyNumber);
            case ZeroXContainerCFA:
            default:
                // If no proper type is found, use the same as the specific
                return makePrimaryInstanceKeyFactory();
        }
    }
//</editor-fold>

//<editor-fold desc="Making n-CFA Objects">

    /**
     * Makes the same context selector as nCFABuilder
     *
     * @param appContextSelector
     * @param val                a value for the n or x in nCFA, ZeroXCFA, ZeroXContainerCFA
     * @return The ContextSelector for nCFA
     */
    protected ContextSelector makeNCFAContextSelector(
            ContextSelector appContextSelector,
            int val) {
        ContextSelector def = new DefaultContextSelector(options, cha);
        ContextSelector contextSelector =
                appContextSelector == null ? def : new DelegatingContextSelector(appContextSelector, def);
        return new nCFAContextSelector(val, contextSelector);
    }

    /**
     * Makes the same context interpreter as nCFABuilder
     *
     * @param appContextInterpreter
     * @return The SSAContextInterpreter for nCFA
     */
    protected SSAContextInterpreter makeNCFAContextInterpreter(
            SSAContextInterpreter appContextInterpreter) {
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

    /**
     * Makes the same InstanceKeyFactory as nCFABuilder
     *
     * @return The InstanceKeyFactory for nCFA
     */
    protected InstanceKeyFactory makeNCFAInstanceKeyFactory() {
        return new ClassBasedInstanceKeys(options, cha);
    }
//</editor-fold>

//<editor-fold desc="Making ZeroX Objects">

    /**
     * Makes the same context selector as ZeroXBuilder
     *
     * @param appContextSelector
     * @return The ContextSelector for ZeroX
     */
    protected ContextSelector makeZeroXContextSelector(
            ContextSelector appContextSelector) {
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
    protected SSAContextInterpreter makeZeroXContextInterpreter(
            SSAContextInterpreter appContextInterpreter) {
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

    /**
     * Makes the same InstanceKeyFactory as ZeroXBuilder
     *
     * @param val the X in ZeroX
     * @return The InstanceKeyFactory for ZeroX
     */
    protected InstanceKeyFactory makeZeroXInstanceKeyFactory(int val) {
        RTAContextInterpreter contextInterpreter = getContextInterpreter();
        if (contextInterpreter == null)
            throw new RuntimeException("ContextInterpreter was null, while creating a ZeroXInstanceKeyFactory");

        return new ZeroXInstanceKeys(options, cha, contextInterpreter, val);
    }
//</editor-fold>

//<editor-fold desc="Making ZeroXContainer Object">

    /**
     * Makes the same context selector as ZeroXContainerBuilder
     *
     * @param appContextSelector
     * @param primaryInstanceKeyFactory Whether the primary instance key factory is the one to get from the delegatingInstanceKeyFactory
     * @return The ContextSelector for ZeroXContainer
     */
    protected ContextSelector makeZeroXContainerContextSelector(ContextSelector appContextSelector, boolean primaryInstanceKeyFactory) {

        SalsaDelegatingInstanceKeyFactory salsaDelegatingInstanceKeyFactory = (SalsaDelegatingInstanceKeyFactory) getInstanceKeys();
        if (salsaDelegatingInstanceKeyFactory == null)
            throw new RuntimeException("InstanceKeyFactory was null while making ZeroXContainerContextSelector");

        ZeroXInstanceKeys zeroXInstanceKeys;
        if (primaryInstanceKeyFactory)
            zeroXInstanceKeys = (ZeroXInstanceKeys) salsaDelegatingInstanceKeyFactory.getPrimaryInstanceKeyFactory();
        else
            zeroXInstanceKeys = (ZeroXInstanceKeys) salsaDelegatingInstanceKeyFactory.getSecondaryInstanceKeyFactory();

        if (zeroXInstanceKeys == null)
            throw new RuntimeException("InstanceKeyFactory delegate was null while making ZeroXContainerContextSelector");

        return new DelegatingContextSelector(
                new ContainerContextSelector(cha, zeroXInstanceKeys),
                makeZeroXContextSelector(appContextSelector));
    }
//</editor-fold>

//<editor-fold desc="Helper Function(s)"

    /**
     * Return the pointers to objects originated from deserialization.
     *
     * @return
     */
    public Set<PointerKey> getDeserializedObjects() {
        return deserializedObjects;
    }

    /**
     * Return the pointers to objects that were serialized in the program.
     *
     * @return
     */
    public Set<PointerKey> getSerializedObjects() {
        return serializedObjects;
    }

    public boolean isSyntheticModel(CGNode node) {
        return models.contains(node);
    }


    //</editor-fold>

}
