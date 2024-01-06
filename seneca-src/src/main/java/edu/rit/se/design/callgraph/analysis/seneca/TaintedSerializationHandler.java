package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.PDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.MonitorUtil;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.intset.OrdinalSet;
import edu.rit.se.design.callgraph.analysis.AbstractSerializationCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.salsa.UnsoundSerializationHandler;
import edu.rit.se.design.callgraph.model.MethodModel;
import edu.rit.se.design.callgraph.util.ModelUtils;
import edu.rit.se.design.callgraph.util.NameUtils;
import edu.rit.se.design.callgraph.util.SerializationUtils;
import edu.rit.se.design.callgraph.util.TypeCategory;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions.NONE;
import static com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions.NO_HEAP_NO_EXCEPTIONS;
import static com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions.REFLECTION;
import static com.ibm.wala.shrikeBT.IInvokeInstruction.Dispatch.VIRTUAL;
import static edu.rit.se.design.callgraph.util.NameUtils.writeObjectCallbackSelector;
import static edu.rit.se.design.callgraph.util.NameUtils.writeReplaceCallbackSelector;
import static edu.rit.se.design.callgraph.util.TypeCategory.OBJECT;


public class TaintedSerializationHandler extends UnsoundSerializationHandler {

    /**
     * Defines the scope of the search for classes that implement serialization callbacks.
     */
    public enum SearchConfig {APPLICATION_ONLY, APP_AND_LIBRARIES, ALL}

    /**
     * Defines the type of collection.
     */
    public enum CollectionType {ARRAY, COLLECTION, MAP, NOT_COLLECTION}

    /**
     * Defines the scope of the search for classes that implement serialization callbacks.
     */
    private final SearchConfig searchConfig = SearchConfig.APP_AND_LIBRARIES;

    public TaintedSerializationHandler(AbstractSerializationCallGraphBuilder senecaCallGraphBuilder) {
        super(senecaCallGraphBuilder);
        if (senecaCallGraphBuilder == null || !(senecaCallGraphBuilder instanceof SenecaCallGraphBuilder))
            throw new IllegalArgumentException("senecaCallGraphBuilder must be an instance of SenecaCallGraphBuilder");

    }

    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

    private boolean isInRightScope(CGNode n) {
        return ((SenecaCallGraphBuilder) this.builder).isNodeInRightScope(n);
    }
//<editor-fold desc="Deserialization of objects">

    /**
     * Makes changes to the models, adding the instructions needed for tricking the pointer analysis to tame with object deserialization.
     *
     * @param changedNodes (acts as an output) it adds to the set nodes that were modified and need to be re-analyzed by the pointer analysis engine.
     * @param monitor      progress monitor (though current implementation does not emit any progress)
     */
    @Override
    protected void handleDeserialization(Set<CGNode> changedNodes, MonitorUtil.IProgressMonitor monitor) {
        for (Triple<CGNode, SSAAbstractInvokeInstruction, CGNode> triple : ((AbstractSerializationCallGraphBuilder) builder).getDeserializationWorkList()) {
            CGNode target = triple.getRight();
            Context context = target.getContext();

            // creates method model
            MethodModel methodModel = ((MethodModel) target.getMethod());
            if (methodModel.isModelComputed(context)) continue;


            ArrayList<Integer> returnValues = new ArrayList<>();
            // gets all application classes
            Stream<IClass> classes = this.serializableClasses.get(ClassLoaderReference.Application).stream();
            // gets all extension classes and primordial classes, if applicable
            if (searchConfig == SearchConfig.ALL || searchConfig == SearchConfig.APP_AND_LIBRARIES) {
                classes = Stream.concat(classes, this.serializableClasses.get(ClassLoaderReference.Extension).stream());
                if (searchConfig == SearchConfig.ALL)
                    classes = Stream.concat(classes, this.serializableClasses.get(ClassLoaderReference.Primordial).stream());
            }

            classes.forEach(klass -> {
                List<IMethod> callbacks = ModelUtils.getDeserializationCallbacks(klass);
                if (callbacks.size() > 0) {
                    SSANewInstruction ssaNewInstruction = methodModel.addAllocation(context, klass.getReference());
                    returnValues.add(ssaNewInstruction.getDef());
                    for (IMethod callback : callbacks) {
                        Selector selector = callback.getSelector();
                        int[] params = selector.equals(NameUtils.readObjectCallbackSelector) ?
                                new int[]{ssaNewInstruction.getDef(), 1}
                                : new int[]{ssaNewInstruction.getDef()};
                        methodModel.addInvocation(context, params, callback.getReference(), VIRTUAL);
                    }
                }
            });


            if (returnValues.size() > 1) {
                SSAPhiInstruction phi = methodModel.addPhi(context, returnValues);
                methodModel.addReturnObject(context, phi.getDef());
            } else if (returnValues.size() == 1) {
                methodModel.addReturnObject(context, returnValues.get(0));
            }


            // invalidate the cache, such that the node can be re-visited for further expansion (if new callbacks were added)
            builder.getAnalysisCache().invalidate(target.getMethod(), target.getContext());

            // mark node as modified to be re-traversed
            changedNodes.add(target);
        }
    }
//</editor-fold>

//<editor-fold desc="Serialization of objects">


    @Override
    protected void handleSerialization(Set<CGNode> changedNodes, MonitorUtil.IProgressMonitor monitor) {
        Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> serializationWorkList = ((AbstractSerializationCallGraphBuilder) builder).getSerializationWorkList();

        for (Triple<CGNode, SSAAbstractInvokeInstruction, CGNode> triple : serializationWorkList) {
            CGNode caller = triple.getLeft();
            SSAAbstractInvokeInstruction call = triple.getMiddle();
            CGNode target = triple.getRight();
            Set<Pair<CGNode, NewSiteReference>> pairs = computeAllocations(caller, call);
            Set<NewSiteReference> allocations = pairs.stream().map(p -> p.snd).collect(Collectors.toSet());
            MethodModel methodModel = ((MethodModel) target.getMethod());
            if (methodModel.isModelComputed(target.getContext())) continue;
            Context context = target.getContext();
            int previousSize = methodModel.getNumberOfStatements(context);


            // add callbacks for the top level object being serialized
            // v2 is the object parameter passed to the method model
            addCallbacksForObject(2, allocations, methodModel, context, caller);


            // check if the object being serialized is a collection
            CollectionType serObjectType = getSerializedObjectType(caller.getClassHierarchy(), allocations);
            // if it is a collection or map, we add the callbacks for its elements
            if (serObjectType == CollectionType.MAP || serObjectType == CollectionType.COLLECTION) {
                Set<Pair<CGNode, NewSiteReference>> addedElements = findElementsAddedToCollection(serObjectType, pairs);
                addInstructionsForCollection(caller, methodModel, context, serObjectType, addedElements);
            } else if (serObjectType == CollectionType.ARRAY) {
                Set<Pair<CGNode, NewSiteReference>> addedElements = findElementsAddedToArray(serObjectType, pairs);
                addInstructionsForArray(caller, methodModel, context, serObjectType, addedElements);
            }

            if (previousSize != methodModel.getNumberOfStatements(context)) {
                // invalidate previous cache
                builder.getAnalysisCache().invalidate(methodModel, context);
                changedNodes.add(target);
            }
        }

    }


    /**
     * Given as input a set of allocations for an object, it checks whether that object is a collection or not.
     * A collection is either an array, or a class that implements the java.util.Collection  or the java.util.Map interfaces.
     *
     * @param cha         class hierarchy
     * @param allocations set of allocations for an object
     * @return true if the object is a collection, false otherwise.
     */
    private static CollectionType getSerializedObjectType(IClassHierarchy cha, Set<NewSiteReference> allocations) {
        Iterator<NewSiteReference> iterator = allocations.iterator();
        if (iterator.hasNext()) {
            TypeReference declaredType = iterator.next().getDeclaredType();
            IClass c = cha.lookupClass(declaredType);
            IClass collectionClass = cha.lookupClass(TypeReference.JavaUtilCollection);
            IClass mapClass = cha.lookupClass(TypeReference.JavaUtilMap);
            // either it is a collection (ex, list, sets, etc.) or a map (Map interface)
            if (declaredType.isArrayType() || c.isArrayClass()) return CollectionType.ARRAY;
            if (cha.isAssignableFrom(collectionClass, c)) return CollectionType.COLLECTION;
            if (cha.isAssignableFrom(mapClass, c)) return CollectionType.MAP;
        }
        return CollectionType.NOT_COLLECTION;
    }

    /**
     * Compute a forward slice for the collection to find all the elements added to the collection.
     *
     * @param serializedObjectType type of the collection (map or collection)
     * @param allocations          allocations of the collection
     * @return set of pairs (node, allocation) of the elements added to the collection
     */
    private Set<Pair<CGNode, NewSiteReference>> findElementsAddedToCollection(CollectionType serializedObjectType, Set<Pair<CGNode, NewSiteReference>> allocations) {
        Set<Pair<CGNode, NewSiteReference>> result = new HashSet<>();
        for (Pair<CGNode, NewSiteReference> pair : allocations) {
            // variable number of the object being deserialized
            CGNode allocationNode = pair.fst;
            NewSiteReference newSiteReference = pair.snd;
            IR allocationNodeIR = allocationNode.getIR();
            SSANewInstruction ssaNewInstruction = allocationNodeIR.getNew(newSiteReference);

            Map<SSAInstruction, Integer> instructionIndices = PDG.computeInstructionIndices(allocationNodeIR);
            Statement st = PDG.ssaInstruction2Statement(allocationNode, ssaNewInstruction, instructionIndices, allocationNodeIR);

            // finds all the places where we added elements
            try {
                Collection<Statement> slice = Slicer.computeForwardSlice(st, builder.getCallGraph(), builder.getPointerAnalysis(), REFLECTION, NONE);
                for (Statement s : slice) {
                    // statement in application scope, and is a normal statement invoking a method
                    if (isInRightScope(s.getNode()) &&
                            s.getKind() == Statement.Kind.NORMAL &&
                            (((NormalStatement) s).getInstruction() instanceof SSAAbstractInvokeInstruction)) {
                        SSAAbstractInvokeInstruction invocation = (SSAAbstractInvokeInstruction) ((NormalStatement) s).getInstruction();
                        String invokedMethodName = invocation.getDeclaredTarget().getName().toString();

                        // the invocation is a call to add or put
                        if (invokedMethodName.startsWith("add") || (invokedMethodName.startsWith("put") && invocation.getNumberOfUses() > 2)) {
                            int varNoObject = serializedObjectType == CollectionType.COLLECTION ? invocation.getUse(1) : invocation.getUse(2);

                            PointerKey pointerKey = builder.getPointerKeyForLocal(s.getNode(), varNoObject);
                            for (InstanceKey instanceKey : builder.getPointerAnalysis().getPointsToSet(pointerKey)) {
                                if (instanceKey instanceof TaintedInstanceKey) continue;
                                Iterator<Pair<CGNode, NewSiteReference>> creationSites = instanceKey.getCreationSites(builder.getCallGraph());
                                while (creationSites.hasNext()) result.add(creationSites.next());
                            }
                        }
                    }
                }
            } catch (CancelException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Compute a forward slice for the array to find all the elements added to the array.
     *
     * @param serializedObjectType type of the collection (map or collection)
     * @param allocations          allocations of the collection
     * @return set of pairs (node, allocation) of the elements added to the collection
     */
    private Set<Pair<CGNode, NewSiteReference>> findElementsAddedToArray(CollectionType serializedObjectType, Set<Pair<CGNode, NewSiteReference>> allocations) {
        Set<Pair<CGNode, NewSiteReference>> result = new HashSet<>();
        for (Pair<CGNode, NewSiteReference> pair : allocations) {
            // variable number of the object being deserialized
            CGNode allocationNode = pair.fst;
            NewSiteReference newSiteReference = pair.snd;
            IR allocationNodeIR = allocationNode.getIR();
            SSANewInstruction ssaNewInstruction = allocationNodeIR.getNew(newSiteReference);

            Map<SSAInstruction, Integer> instructionIndices = PDG.computeInstructionIndices(allocationNodeIR);
            Statement st = PDG.ssaInstruction2Statement(allocationNode, ssaNewInstruction, instructionIndices, allocationNodeIR);

            // finds all the places where we added elements
            try {
                Collection<Statement> slice = Slicer.computeForwardSlice(st, builder.getCallGraph(), builder.getPointerAnalysis(), NO_HEAP_NO_EXCEPTIONS, NONE);
                for (Statement s : slice) {
                    if (!isInRightScope(s.getNode()) || s.getKind() != Statement.Kind.NORMAL) continue;
                    SSAInstruction instruction = ((NormalStatement) s).getInstruction();
                    if (!(instruction instanceof SSAArrayStoreInstruction)) continue;
                    int varNoObject = instruction.getUse(2);
                    PointerKey pointerKey = builder.getPointerKeyForLocal(s.getNode(), varNoObject);
                    for (InstanceKey instanceKey : builder.getPointerAnalysis().getPointsToSet(pointerKey)) {
                        if (instanceKey instanceof TaintedInstanceKey) continue;
                        Iterator<Pair<CGNode, NewSiteReference>> creationSites = instanceKey.getCreationSites(builder.getCallGraph());
                        while (creationSites.hasNext()) result.add(creationSites.next());
                    }
                }
            } catch (CancelException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * Given as input a variable number of an object being deserialized,
     * this method adds all of its callbacks to the synthetic model method.
     *
     * @param vn          a variable number of the object being serialized.
     * @param allocations the set of allocation sites of the object being serialized.
     */
    private void addCallbacksForObject(int vn, Set<NewSiteReference> allocations, MethodModel methodModel, Context context, CGNode caller) {
        for (NewSiteReference site : allocations) {
            TypeReference topLevelReference = site.getDeclaredType();
            IClass topLevelClass = builder.getClassHierarchy().lookupClass(topLevelReference);
            // vCastNo = (T) vn
            int vCastNo = methodModel.addCheckcast(context, new TypeReference[]{topLevelReference}, vn, true);

            // if class has writeReplace() callback, we add vCastNo.writeReplace()
            IMethod writeReplaceCallbackMethod = topLevelClass.getMethod(writeReplaceCallbackSelector);
            if (writeReplaceCallbackMethod != null)
                methodModel.addInvocation(context, new int[]{vCastNo}, writeReplaceCallbackMethod.getReference(), VIRTUAL);

            // if class has writeObject() callback, we add vCastNo.writeObject()
            IMethod writeObjectCallbackMethod = topLevelClass.getMethod(writeObjectCallbackSelector);
            if (writeObjectCallbackMethod != null)
                methodModel.addInvocation(context, new int[]{vCastNo, 1}, writeObjectCallbackMethod.getReference(), VIRTUAL);

            // handles inner (non-static) fields, including from super classes
            TypeCategory typeCategory = SerializationUtils.getTypeCategory(builder.cha, topLevelReference);
            if (typeCategory == OBJECT)
                handleInnerClassObjects(caller, context, methodModel, site, topLevelClass, vCastNo);
        }
    }

    private void addInstructionsForArray(CGNode caller, MethodModel methodModel, Context context, CollectionType serObjectType, Set<Pair<CGNode, NewSiteReference>> addedElements) {
        int vObject = 2; // v2 is the object parameter passed to the method model (the object being serialized)
        // variable number for indexing the array
        int vIndex = methodModel.getValueNumberForIntConstant(context, 0);

        for (Pair<CGNode, NewSiteReference> addedElement : addedElements) {
            // add array load instruction: v_element = v_obj[v_index]
            int vElement = methodModel.addArrayLoad(context, addedElement.snd.getDeclaredType(), vObject, vIndex);
            // add callbacks for the added elements
            addCallbacksForObject(vElement, addedElements.stream().map(p -> p.snd).collect(Collectors.toSet()), methodModel, context, caller);
        }
    }

    /**
     * Creates the needed instructions for the collection being serialized.
     * It adds checkcasts and invocations to the synthetic method model to mimic the behavior of invoking its inner callbacks.
     *
     * @param caller
     * @param methodModel
     * @param context
     * @param serObjectType
     * @param addedElements
     */
    private void addInstructionsForCollection(CGNode caller, MethodModel methodModel, Context context, CollectionType serObjectType, Set<Pair<CGNode, NewSiteReference>> addedElements) {
        int vIterator = 2; // v2 is the object parameter passed to the method model (the object being serialized)

        if (serObjectType == CollectionType.MAP) {
            // add call: v2.values()Ljava/util/Collection;
            MethodReference valuesMethod = MethodReference.findOrCreate(TypeReference.JavaUtilMap, Selector.make("values()Ljava/util/Collection;"));
            SSAAbstractInvokeInstruction valuesInvoke = methodModel.addInvocation(context, new int[]{vIterator}, valuesMethod, VIRTUAL);
            vIterator = valuesInvoke.getDef();
        }


        // add call: vIterator.iterator()Ljava/util/Iterator;
        MethodReference iteratorMethod = MethodReference.findOrCreate(TypeReference.JavaUtilCollection, Selector.make("iterator()Ljava/util/Iterator;"));
        SSAAbstractInvokeInstruction iteratorInvoke = methodModel.addInvocation(context, new int[]{vIterator}, iteratorMethod, VIRTUAL);
        // add call: vIterator.iterator().next()Ljava/lang/Object;
        MethodReference nextMethod = MethodReference.findOrCreate(TypeReference.JavaUtilIterator, Selector.make("next()Ljava/lang/Object;"));
        SSAAbstractInvokeInstruction nextInvoke = methodModel.addInvocation(context, new int[]{iteratorInvoke.getDef()}, nextMethod, VIRTUAL);


        for (Pair<CGNode, NewSiteReference> addedElement : addedElements) {
            // add checkcast: (T) v2.iterator().next()
            TypeReference[] types = new TypeReference[]{addedElement.snd.getDeclaredType()};
            int vCastNo = methodModel.addCheckcast(context, types, nextInvoke.getDef(), true);

            // add callbacks for the added elements
            addCallbacksForObject(vCastNo, addedElements.stream().map(p -> p.snd).collect(Collectors.toSet()), methodModel, context, caller);
        }
    }


    /**
     * Computes the concrete types of the objects being serialized.
     *
     * @param caller
     * @param call
     * @return
     * @throws CancelException
     */
    private Set<Pair<CGNode, NewSiteReference>> computeAllocations(CGNode caller, SSAAbstractInvokeInstruction call) {
        Set<Pair<CGNode, NewSiteReference>> result = new HashSet<>();
        int varNoObject = call.getUse(1); // variable number for the object being serialized
        PointerKey pkParameter = builder.getPointerKeyForLocal(caller, varNoObject);
        OrdinalSet<InstanceKey> pointsToSet = builder.getPointerAnalysis().getPointsToSet(pkParameter);

        pointsToSet.forEach(instanceKey -> {
            if (!(instanceKey instanceof TaintedInstanceKey)) {
                Iterator<Pair<CGNode, NewSiteReference>> creationSites = instanceKey.getCreationSites(builder.getCallGraph());
                while (creationSites.hasNext()) {
                    Pair<CGNode, NewSiteReference> next = creationSites.next();
                    if (isInRightScope(next.fst))
                        result.add(next);
                }
            }
        });
        return result.isEmpty() ? computeAllocationsFromSlice(caller, call) : result;
    }

    /**
     * Computes the concrete types of the objects being serialized by slicing the call graph.
     * This is invoked in a situation where WALA conflated the new sites of the objects being serialized.
     * This happens when the object being serialized is a collection or a map.
     * In this case, we slice the call graph to find the concrete types of the objects being serialized on the actual application scope.
     *
     * @param caller the caller node
     * @param call   the call instruction to the serialization method (ie, writeObject)
     * @return the set of concrete types of the objects being serialized with its creation site (node and new site reference)
     */
    private Set<Pair<CGNode, NewSiteReference>> computeAllocationsFromSlice(CGNode caller, SSAAbstractInvokeInstruction call) {
        IClassHierarchy cha = caller.getClassHierarchy();
        IClass collectionClass = cha.lookupClass(TypeReference.JavaUtilCollection);
        IClass mapClass = cha.lookupClass(TypeReference.JavaUtilMap);
        Set<Pair<CGNode, NewSiteReference>> result = new HashSet<>();
        try {
            NormalStatement normalStatement = new NormalStatement(caller, call.iIndex());
            Collection<Statement> statements = Slicer.computeBackwardSlice(normalStatement, builder.getCallGraph(), builder.getPointerAnalysis(), REFLECTION, NONE);
            for (Statement statement : statements) {
                if (!isInRightScope(statement.getNode()) || !(statement instanceof NormalStatement))
                    continue;
                if (!(((NormalStatement) statement).getInstruction() instanceof SSANewInstruction)) continue;
                SSANewInstruction newInstruction = (SSANewInstruction) ((NormalStatement) statement).getInstruction();

                // this is a band-aid YOLO
                IClass c = cha.lookupClass(newInstruction.getConcreteType());
                if (c != null && (cha.isAssignableFrom(collectionClass, c) || cha.isAssignableFrom(mapClass, c))) {
                    result.add(Pair.make(statement.getNode(), newInstruction.getNewSite()));
                }
            }

        } catch (CancelException e) {
            e.printStackTrace();
        }
        return result;
    }

//</editor-fold>
}
