package edu.rit.se.design.callgraph.analysis.salsa;


import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.slicer.NormalReturnCaller;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.generics.ArrayTypeSignature;
import com.ibm.wala.types.generics.ClassTypeSignature;
import com.ibm.wala.types.generics.TypeArgument;
import com.ibm.wala.types.generics.TypeSignature;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.MonitorUtil;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.intset.OrdinalSet;
import edu.rit.se.design.callgraph.analysis.AbstractSerializationCallGraphBuilder;
import edu.rit.se.design.callgraph.analysis.AbstractSerializationHandler;
import edu.rit.se.design.callgraph.analysis.seneca.TaintedInstanceKey;
import edu.rit.se.design.callgraph.model.MethodModel;
import edu.rit.se.design.callgraph.util.ModelUtils;
import edu.rit.se.design.callgraph.util.NameUtils;
import edu.rit.se.design.callgraph.util.SerializationUtils;
import edu.rit.se.design.callgraph.util.TypeCategory;
import org.apache.commons.lang3.tuple.Triple;

import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions.NONE;
import static com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions.REFLECTION;
import static com.ibm.wala.shrikeBT.IInvokeInstruction.Dispatch.VIRTUAL;
import static edu.rit.se.design.callgraph.util.NameUtils.*;
import static edu.rit.se.design.callgraph.util.TypeCategory.*;

/**
 * This class implements support for serialization-related features.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class UnsoundSerializationHandler extends AbstractSerializationHandler {
    private boolean prune = true;
    private int pruningThreshold = 100;

    public UnsoundSerializationHandler(AbstractSerializationCallGraphBuilder builder) {
        super(builder);
    }

    @Override
    public void handleSerializationRelatedFeatures(MonitorUtil.IProgressMonitor monitor) {
        try {
            Set<CGNode> changedNodes = HashSetFactory.make();
            handleDeserialization(changedNodes, monitor);
            ((AbstractSerializationCallGraphBuilder) builder).getDeserializationWorkList().clear();
            handleSerialization(changedNodes, monitor);
            ((AbstractSerializationCallGraphBuilder) builder).getSerializationWorkList().clear();
            // tag them as changed, so it can go through again the visiting process
            for (CGNode cgNode : changedNodes) builder.addConstraintsFromChangedNode(cgNode, monitor);
        } catch (CancelException e) {
            throw new RuntimeException(e);
        }
    }

//<editor-fold desc="Serialization of objects">

    /**
     * Makes changes to the models, adding the instructions needed for tricking the pointer analysis to tame with object serialization.
     *
     * @param changedNodes (acts as an output) it adds to the set nodes that were modified and need to be re-analyzed by the pointer analysis engine.
     * @param monitor      progress monitor (though current implementation does not emit any progress)
     */
    protected void handleSerialization(Set<CGNode> changedNodes, MonitorUtil.IProgressMonitor monitor) {
        Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> serializationWorkList = ((AbstractSerializationCallGraphBuilder) builder).getSerializationWorkList();
        for (Triple<CGNode, SSAAbstractInvokeInstruction, CGNode> triple : serializationWorkList) {
            CGNode caller = triple.getLeft();
            SSAAbstractInvokeInstruction call = triple.getMiddle();
            CGNode target = triple.getRight();

            Set<NewSiteReference> allocations = getAllocatedParameterType(caller, call);
            MethodModel methodModel = ((MethodModel) target.getMethod());
            Context context = target.getContext();
            int previousSize = methodModel.getNumberOfStatements(context);


            int topLevelObjectNumber = 2; // v2 is the object parameter passed to the method model
            for (NewSiteReference site : allocations) {
                TypeReference topLevelReference = site.getDeclaredType();
                IClass topLevelClass = builder.getClassHierarchy().lookupClass(topLevelReference);
                // vCast = (T) v2
                int topLevelCast = methodModel.addCheckcast(context, new TypeReference[]{topLevelReference}, topLevelObjectNumber, true);

                // if class has writeReplace() callback, we mimic its invocation
                IMethod writeReplaceCallbackMethod = topLevelClass.getMethod(writeReplaceCallbackSelector);
                if (writeReplaceCallbackMethod != null) {
                    // vCast.writeReplace()
                    methodModel.addInvocation(context, new int[]{topLevelCast}, writeReplaceCallbackMethod.getReference(), VIRTUAL);
                }

                // if class has writeObject() callback, we mimic its invocation
                IMethod writeObjectCallbackMethod = topLevelClass.getMethod(writeObjectCallbackSelector);
                if (writeObjectCallbackMethod != null) {
                    // vCast.writeObject(v1)
                    methodModel.addInvocation(context, new int[]{topLevelCast, 1}, writeObjectCallbackMethod.getReference(), VIRTUAL);
                }
                // handles inner (non-static) fields, including from super classes
                TypeCategory typeCategory = SerializationUtils.getTypeCategory(builder.cha, topLevelReference);
                if (typeCategory == OBJECT)
                    handleInnerClassObjects(caller, context, methodModel, site, topLevelClass, topLevelCast);
            }
            if (previousSize != methodModel.getNumberOfStatements(context)) {
                // invalidate previous cache
                builder.getAnalysisCache().invalidate(methodModel, context);
                changedNodes.add(target);
            }
        }
    }


    protected void handleInnerClassObjects(CGNode caller, Context context, MethodModel methodModel, NewSiteReference site, IClass topLevelClass, int vCastNo) {
        Collection<IField> allInstanceFields = topLevelClass.getAllInstanceFields();
        InstanceKey topObjIk = builder.getInstanceKeyForAllocation(caller, site);
        for (IField iField : allInstanceFields) {
            PointerKey pkForField = builder.getPointerKeyForInstanceField(topObjIk, iField);
            OrdinalSet<InstanceKey> concreteFieldTypes = builder.getPointerAnalysis().getPointsToSet(pkForField);
            // iterate the allocated types for the inner fields
            for (InstanceKey concreteFieldType : concreteFieldTypes) {
                // TODO: add more callbacks
                IMethod innerCallbackMethod = concreteFieldType.getConcreteType().getMethod(writeObjectCallbackSelector);
                if (innerCallbackMethod != null) {
                    // vx = checkcast (ConcreteType)vCast
                    int varInnerField = methodModel.addGetInstance(context, iField.getReference(), vCastNo);
                    int varInnerCast = methodModel.addCheckcast(context, new TypeReference[]{concreteFieldType.getConcreteType().getReference()}, varInnerField, true);
                    // we add the equivalent of v<innerCast>.writeObject(v1)
                    methodModel.addInvocation(context, new int[]{varInnerCast, 1}, innerCallbackMethod.getReference(), VIRTUAL);
                }
            }
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
    protected Set<NewSiteReference> getAllocatedParameterType(CGNode caller, SSAAbstractInvokeInstruction call) {
        Set<NewSiteReference> result = new HashSet<>();
        int varNoObject = call.getUse(1); // variable number for the object being serialized
        PointerKey pkParameter = builder.getPointerKeyForLocal(caller, varNoObject);
        OrdinalSet<InstanceKey> pointsToSet = builder.getPointerAnalysis().getPointsToSet(pkParameter);

        pointsToSet.forEach(instanceKey -> {
            if (!(instanceKey instanceof TaintedInstanceKey)) {
                Iterator<Pair<CGNode, NewSiteReference>> creationSites = instanceKey.getCreationSites(builder.getCallGraph());
                creationSites.forEachRemaining(pair -> {
                    result.add(pair.snd);
                });
            }
        });
        return result;
    }
//</editor-fold>


//<editor-fold desc="Deserialization of objects">

    /**
     * Makes changes to the models, adding the instructions needed for tricking the pointer analysis to tame with object deserialization.
     *
     * @param changedNodes (acts as an output) it adds to the set nodes that were modified and need to be re-analyzed by the pointer analysis engine.
     * @param monitor      progress monitor (though current implementation does not emit any progress)
     */
    protected void handleDeserialization(Set<CGNode> changedNodes, MonitorUtil.IProgressMonitor monitor) throws CancelException {

        for (Triple<CGNode, SSAAbstractInvokeInstruction, CGNode> triple : ((AbstractSerializationCallGraphBuilder) builder).getDeserializationWorkList()) {
            CGNode caller = triple.getLeft();
            SSAAbstractInvokeInstruction call = triple.getMiddle();
            CGNode target = triple.getRight();

            // compute slices to find downcasts
            NormalReturnCaller st = new NormalReturnCaller(caller, call.iIndex());
            Collection<Statement> slice = Slicer.computeForwardSlice(st, builder.getCallGraph(), builder.getPointerAnalysis(), REFLECTION, NONE);
            Set<SSACheckCastInstruction> casts = slice.stream()
                    .filter(s ->
                            s.getKind() == Statement.Kind.NORMAL &&
                                    (((NormalStatement) s).getInstruction() instanceof SSACheckCastInstruction))
                    .map(s -> (SSACheckCastInstruction) ((NormalStatement) s).getInstruction())
                    .collect(Collectors.toSet());

            ArrayList<Integer> returnValues = new ArrayList<>();

            // if there are no downcasts, then we compute the set of all possibilities
            if (casts.isEmpty()) {
                // if there are no casts, finds all application classes
                this.serializableClasses.get(ClassLoaderReference.Application)
                        .forEach(klass -> {
                            List<IMethod> callbacks = ModelUtils.getDeserializationCallbacks(klass);
                            if (callbacks.size() > 0) {
                                computeMethodModelInstructions(caller, target, returnValues, klass);
                            }
                        });
            } else {
                // if there are casts, narrows down to cast only information
                for (SSACheckCastInstruction cast : casts) {
                    Arrays.asList(cast.getDeclaredResultTypes()).stream()
                            .map(typeReference -> builder.getClassHierarchy().lookupClass(typeReference))
                            .filter(klass -> klass != null)
                            .forEach(c -> {
                                Collection<IClass> subClasses = builder.cha.computeSubClasses(c.getReference());
                                for (IClass klass : subClasses) {
                                    computeMethodModelInstructions(caller, target, returnValues, klass);
                                }
                            });
                }
            }
            MethodModel methodModel = (MethodModel) target.getMethod();
            Context context = target.getContext();
            if (returnValues.size() > 1) {
                SSAPhiInstruction phi = methodModel.addPhi(context, returnValues);
                methodModel.addReturnObject(context, phi.getDef());
            } else if (returnValues.size() == 1) {
                methodModel.addReturnObject(context, returnValues.get(0));
            } else {
                // this is a perfectly acceptable scenario where no serializable classes with callbacks were found!
            }
            // invalidate the cache
            builder.getAnalysisCache().invalidate(target.getMethod(), context);

            // mark node as modified to be re-traversed
            changedNodes.add(target);
        }
    }

    /**
     * Computes on-the-fly the instructions to be added to a deserialization method model ({@link ObjectInputStream#readObject()}).
     *
     * @param caller          method who invoked OIS.readObject()
     * @param methodModelNode the call graph node that is synthetic (method model, i.e. salsa.readObject())
     * @param returnValues    a reference to an array list in which we add return values.
     * @param klass
     */
    private SSANewInstruction computeMethodModelInstructions(CGNode caller, CGNode methodModelNode, ArrayList<Integer> returnValues, IClass klass) {
        MethodModel methodModel = ((MethodModel) methodModelNode.getMethod());
        Context context = methodModelNode.getContext();

        SSANewInstruction ssaNewInstruction = methodModel.addAllocation(context, klass.getReference());
        returnValues.add(ssaNewInstruction.getDef());

        // if class implements the readObject() callback, invokes it
        IMethod readObjectCallbackMethod = klass.getMethod(NameUtils.readObjectCallbackSelector);
        if (readObjectCallbackMethod != null) {
            methodModel.addInvocation(context, new int[]{ssaNewInstruction.getDef(), 1}, readObjectCallbackMethod.getReference(), VIRTUAL);
        }

        // if class implements the readObjectNoData() callback, invokes it
        IMethod readObjectNoDataCallbackMethod = klass.getMethod(readObjectNoDataCallbackSelector);
        if (readObjectNoDataCallbackMethod != null) {
            methodModel.addInvocation(context, new int[]{ssaNewInstruction.getDef()/*, 1*/}, readObjectNoDataCallbackMethod.getReference(), VIRTUAL);
        }

        // if class implements the readResolve() callback, invokes it
        IMethod readResolveCallbackMethod = klass.getMethod(NameUtils.readResolveCallbackSelector);
        if (readResolveCallbackMethod != null) {
            methodModel.addInvocation(context, new int[]{ssaNewInstruction.getDef()}, readResolveCallbackMethod.getReference(), VIRTUAL);
        }

        // if class implements the validateObject() callback, invokes it
        IMethod validateObjectCallbackMethod = klass.getMethod(validateObjectCallbackSelector);
        if (validateObjectCallbackMethod != null) {
            methodModel.addInvocation(context, new int[]{ssaNewInstruction.getDef()}, validateObjectCallbackMethod.getReference(), VIRTUAL);
        }


        // handles inner (non-static) fields, including from super classes
        TypeCategory typeCategory = SerializationUtils.getTypeCategory(builder.cha, klass.getReference());
        if (typeCategory == OBJECT)
            handleInnerDeserializedObjects(caller, context, methodModelNode, klass, ssaNewInstruction);
        return ssaNewInstruction;
    }


    protected void handleInnerDeserializedObjects(CGNode caller, Context context, CGNode target, IClass klass, SSANewInstruction ssaNewInstruction) {
        MethodModel methodModel = ((MethodModel) target.getMethod());
        Collection<IField> allInstanceFields = klass.getAllInstanceFields();
        InstanceKey topObjIk = builder.getInstanceKeyForAllocation(caller, ssaNewInstruction.getNewSite());
        if (topObjIk == null) return;//Assertions.UNREACHABLE("topObjIk should not be null"); //FIXME
        TypeCategory classCategory = SerializationUtils.getTypeCategory(builder.cha, klass.getReference());
        boolean isCollection = classCategory == LIST || classCategory == MAP || classCategory == SET;

        for (IField iField : allInstanceFields) {
            TypeReference fieldTypeRef = iField.getFieldTypeReference();
            if (fieldTypeRef.isPrimitiveType()) continue;
            IClass iFieldStaticType = builder.cha.lookupClass(fieldTypeRef);
            if (isCollection) iFieldStaticType = extractGenericType(iField);

            Set<IClass> possibleTypes = SerializationUtils.computePossibleTypes(builder.cha, klass, iFieldStaticType, serializableClasses, prune, pruningThreshold);
            for (IClass concreteFieldType : possibleTypes) {
                int varInnerField = -1, varInnerCast = -1;

                // if class implements the readObject() callback, invokes it
                IMethod readObjectCallbackMethod = klass.getMethod(NameUtils.readObjectCallbackSelector);
                if (readObjectCallbackMethod != null) {
                    // vX = vY.field
                    varInnerField = methodModel.addGetInstance(context, iField.getReference(), ssaNewInstruction.getDef());
                    builder.getSystem().newConstraint(builder.getPointerKeyForLocal(target, varInnerField), new ConcreteTypeKey(concreteFieldType));
                    // vZ = (ConcreteType) vX
                    varInnerCast = methodModel.addCheckcast(context, new TypeReference[]{concreteFieldType.getReference()}, varInnerField, true);
                    // we add the equivalent of vZ.readObject(ois = v1)
                    methodModel.addInvocation(context, new int[]{varInnerCast, 1}, readObjectCallbackMethod.getReference(), VIRTUAL);
                }

                // if class implements the readObjectNoData() callback, invokes it
                IMethod readObjectNoDataCallbackMethod = klass.getMethod(readObjectNoDataCallbackSelector);
                if (readObjectNoDataCallbackMethod != null) {
                    if (varInnerField < 0) {
                        // vX = vY.field
                        varInnerField = methodModel.addGetInstance(context, iField.getReference(), ssaNewInstruction.getDef());
                        builder.getSystem().newConstraint(builder.getPointerKeyForLocal(target, varInnerField), new ConcreteTypeKey(concreteFieldType));
                        // vZ = (ConcreteType) vX
                        varInnerCast = methodModel.addCheckcast(context, new TypeReference[]{concreteFieldType.getReference()}, varInnerField, true);
                    }

                    methodModel.addInvocation(context, new int[]{varInnerCast/*, 1*/}, readObjectNoDataCallbackMethod.getReference(), VIRTUAL);
                }

                // if class implements the readResolve() callback, invokes it
                IMethod readResolveCallbackMethod = klass.getMethod(NameUtils.readResolveCallbackSelector);
                if (readResolveCallbackMethod != null) {
                    if (varInnerField < 0) {
                        // vX = vY.field
                        varInnerField = methodModel.addGetInstance(context, iField.getReference(), ssaNewInstruction.getDef());
                        builder.getSystem().newConstraint(builder.getPointerKeyForLocal(target, varInnerField), new ConcreteTypeKey(concreteFieldType));
                        // vZ = (ConcreteType) vX
                        varInnerCast = methodModel.addCheckcast(context, new TypeReference[]{concreteFieldType.getReference()}, varInnerField, true);
                    }
                    methodModel.addInvocation(context, new int[]{varInnerCast}, readResolveCallbackMethod.getReference(), VIRTUAL);
                }

                // if class implements the validateObject() callback, invokes it
                IMethod validateObjectCallbackMethod = klass.getMethod(validateObjectCallbackSelector);
                if (validateObjectCallbackMethod != null) {
                    if (varInnerField < 0) {
                        // vX = vY.field
                        varInnerField = methodModel.addGetInstance(context, iField.getReference(), ssaNewInstruction.getDef());
                        builder.getSystem().newConstraint(builder.getPointerKeyForLocal(target, varInnerField), new ConcreteTypeKey(concreteFieldType));
                        // vZ = (ConcreteType) vX
                        varInnerCast = methodModel.addCheckcast(context, new TypeReference[]{concreteFieldType.getReference()}, varInnerField, true);
                    }
                    methodModel.addInvocation(context, new int[]{varInnerCast}, validateObjectCallbackMethod.getReference(), VIRTUAL);
                }
            }
        }
    }

    protected IClass extractGenericType(IField iField) {
        if (iField != null && iField instanceof FieldImpl) {
            TypeSignature genericSignature = ((FieldImpl) iField).getGenericSignature();
            if (genericSignature != null) {
                if (genericSignature.isClassTypeSignature()) {
                    TypeArgument typeArgument = ((ClassTypeSignature) genericSignature).getTypeArguments()[0];
                    TypeSignature fieldTypeSignature = typeArgument.getFieldTypeSignature();
                    if (fieldTypeSignature != null && fieldTypeSignature.isClassTypeSignature())
                        return ClassTypeSignature.lookupClass(builder.cha, (ClassTypeSignature) fieldTypeSignature);
                } else if (genericSignature.isArrayTypeSignature()) {
                    TypeSignature contents = ((ArrayTypeSignature) genericSignature).getContents();
                }
            }
        }
        return builder.cha.lookupClass(iField.getFieldTypeReference());
    }


//</editor-fold>
}
