package edu.rit.se.design.callgraph.model;

import com.ibm.wala.cfg.InducedCFG;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.SyntheticIR;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.warnings.Warning;
import com.ibm.wala.util.warnings.Warnings;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;


/**
 * Generic class for synthetic (model) methods of {@link AbstractClassModel}.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class MethodModel extends SyntheticMethod {

    public final IClassHierarchy cha;
    protected final IAnalysisCacheView cache;
    protected final SSAInstructionFactory instructionFactory;
    private final AnalysisOptions options;
    private final int initialNextLocal;
    protected TypeReference[] exceptions;

    //    private final List<SSAInstruction> statements;
//    private Map<ConstantValue, Integer> constant2ValueNumber = HashMapFactory.make();
//    public int nextLocal;
    // <statements, constant2ValueNumber, nextLocal>
    private Map<Context, Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer>> statementsPerContext;


    public MethodModel(Selector selector, TypeReference[] declaredExceptions, IClass declaringClass, IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache) {
        super(MethodReference.findOrCreate(declaringClass.getReference(), selector), declaringClass, false, false);
        if (cache == null || cha == null || declaredExceptions == null)
            throw new IllegalArgumentException("cha, cache and declaredExceptions parameters cannot be null");
        this.cha = cha;
        this.options = options;
        this.cache = cache;
        this.instructionFactory = declaringClass.getClassLoader().getInstructionFactory();
        this.exceptions = declaredExceptions;
        this.initialNextLocal = getNumberOfParameters() + declaredExceptions.length + 1;
        this.statementsPerContext = new HashMap<>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public SSAInstruction[] getStatements(SSAOptions options) {
        return NO_STATEMENTS;
    }


    public SSAInstruction[] getStatements(Context c) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        SSAInstruction[] instructions = new SSAInstruction[statements.size()];
        statements.toArray(instructions);
        return instructions;
    }

    public boolean isModelComputed(Context c) {
        return statementsPerContext.containsKey(c);
    }

    @Override
    public IR makeIR(Context context, SSAOptions options) {
//        System.out.println("Invoking " + getClass().getSimpleName() + ".makeIR (ctx=" + context + ")");
//        IR ir = this.cache.getIR(this, context);
//        if (ir == null) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(context, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        SSAInstruction[] instructions = new SSAInstruction[statements.size()];
        statements.toArray(instructions);
        Map<Integer, ConstantValue> constants = null;
        Map<ConstantValue, Integer> constant2ValueNumber = triple.getMiddle();
        if (!constant2ValueNumber.isEmpty()) {
            constants = HashMapFactory.make(constant2ValueNumber.size());
            for (Map.Entry<ConstantValue, Integer> entry : constant2ValueNumber.entrySet()) {
                constants.put(entry.getValue(), entry.getKey());
            }
        }
        InducedCFG cfg = makeControlFlowGraph(instructions);/*this.getDeclaringClass()
                .getClassLoader()
                .getLanguage()
                .makeInducedCFG(instructions, this, context);*/
        IR ir = new SyntheticIR(this, context, cfg, instructions, options, constants);
//        }

        return ir;
    }

//    public int addLocal(Context c) {
//        return nextLocal++;
//    }

    /**
     * @param c
     * @param resultVar   variable number that is being returned
     * @param isPrimitive if returned value is a primitive
     * @return
     */
    public SSAReturnInstruction addReturn(Context c, int resultVar, boolean isPrimitive) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        SSAReturnInstruction s = this.instructionFactory.ReturnInstruction(statements.size(), resultVar, isPrimitive);
        statements.add(s);

        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(statements, triple.getMiddle(), triple.getRight()));
        return s;
    }

    /**
     * @param c
     * @param resultVar variable number that is being returned
     * @return
     */
    public SSAReturnInstruction addReturnObject(Context c, int resultVar) {
        return this.addReturn(c, resultVar, false);
    }

    /**
     * Adds a method invocation instruction to this synthetic method.
     *
     * @return the invoke instructions added by this operation
     * @throws IllegalArgumentException if site is null
     */
    public SSAAbstractInvokeInstruction addInvocation(Context c, int[] params, MethodReference target, IInvokeInstruction.IDispatch invocationCode) {
        if (target == null || invocationCode == null) {
            throw new IllegalArgumentException("target and/or invocationCode is/are null");
        }
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        int nextLocal = triple.getRight();
        CallSiteReference newSite = CallSiteReference.make(statements.size(), target, invocationCode);
        SSAAbstractInvokeInstruction s = (newSite.getDeclaredTarget().getReturnType().equals(TypeReference.Void)) ?
                this.instructionFactory.InvokeInstruction(statements.size(), params, nextLocal++, newSite, null) :
                this.instructionFactory.InvokeInstruction(statements.size(), nextLocal++, params, nextLocal++, newSite, null);
        statements.add(s);

        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(statements, triple.getMiddle(), nextLocal));
        return s;
    }

    /**
     * Add a New statement of the given type
     *
     * <p>Side effect: adds call to default constructor of given type if one exists.
     *
     * @return instruction added, or null
     * @throws IllegalArgumentException if T is null
     */
    public SSANewInstruction addAllocation(Context c, TypeReference T) {
        return addAllocation(c, T, true);
    }

//    /**
//     * Add a New statement of the given array type and length
//     */
//    public SSANewInstruction add1DArrayAllocation(TypeReference T, int length) {
//        int instance = nextLocal++;
//        NewSiteReference ref = NewSiteReference.make(statements.size(), T);
//        assert T.isArrayType();
//        assert ((ArrayClass) cha.lookupClass(T)).getDimensionality() == 1;
//        int[] sizes = new int[1];
//        Arrays.fill(sizes, getValueNumberForIntConstant(length));
//        SSANewInstruction result = instructionFactory.NewInstruction(statements.size(), instance, ref, sizes);
//        statements.add(result);
////        cache.invalidate(this, Everywhere.EVERYWHERE);
//        return result;
//    }

//    /**
//     * Add a New statement of the given type
//     */
//    public SSANewInstruction addAllocationWithoutCtor(TypeReference T) {
//        return addAllocation(T, false);
//    }

    /**
     * Add a New statement of the given type
     *
     * @param ctx        current context
     * @param typeRef    type being allocated
     * @param invokeCtor if the closest non-serializable default constructor should be invoked
     * @return instruction added, or null
     * @throws IllegalArgumentException if typeRef is null
     */
    private SSANewInstruction addAllocation(Context ctx, TypeReference typeRef, boolean invokeCtor) {
        if (typeRef == null) {
            throw new IllegalArgumentException("typeRef is null");
        }

        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(ctx, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        Integer nextLocal = triple.getRight();

        int instance = nextLocal++;
        SSANewInstruction result = null;

        if (typeRef.isReferenceType()) {
            NewSiteReference ref = NewSiteReference.make(statements.size(), typeRef);
            if (typeRef.isArrayType()) {
                int[] sizes = new int[ArrayClass.getArrayTypeDimensionality(typeRef)];
                Arrays.fill(sizes, getValueNumberForIntConstant(ctx, 1));
                result = instructionFactory.NewInstruction(statements.size(), instance, ref, sizes);
            } else {
                result = instructionFactory.NewInstruction(statements.size(), instance, ref);
            }
            statements.add(result);

            IClass klass = cha.lookupClass(typeRef);
            if (klass == null) {
                Warnings.add(AllocationFailure.create(typeRef));
                return null;
            }

            if (klass.isArrayClass()) {
                int arrayRef = result.getDef();
                TypeReference e = klass.getReference().getArrayElementType();
                while (e != null && !e.isPrimitiveType()) {
                    // allocate an instance for the array contents
                    NewSiteReference n = NewSiteReference.make(statements.size(), e);
                    int alloc = nextLocal++;
                    SSANewInstruction ni = null;
                    if (e.isArrayType()) {
                        int[] sizes = new int[((ArrayClass) cha.lookupClass(typeRef)).getDimensionality()];
                        Arrays.fill(sizes, getValueNumberForIntConstant(ctx, 1));
                        ni = instructionFactory.NewInstruction(statements.size(), alloc, n, sizes);
                    } else {
                        ni = instructionFactory.NewInstruction(statements.size(), alloc, n);
                    }
                    statements.add(ni);

                    // emit an astore
                    SSAArrayStoreInstruction store =
                            instructionFactory.ArrayStoreInstruction(
                                    statements.size(), arrayRef, getValueNumberForIntConstant(ctx, 0), alloc, e);
                    statements.add(store);

                    e = e.isArrayType() ? e.getArrayElementType() : null;
                    arrayRef = alloc;
                }
            }
            // update the inner cache
            statementsPerContext.put(ctx, new ImmutableTriple<>(statements, triple.getMiddle(), nextLocal));

            if (invokeCtor) {
//                // find the closest default constructor
//                IMethod constructor = null;
//                IClass superclass = klass.getSuperclass();
//                IClass serialInterface = cha.lookupClass(TypeReference.JavaIoSerializable);
//
//                while (constructor == null && superclass != null) {
//                    if (!cha.implementsInterface(superclass, serialInterface))
//                        constructor = cha.resolveMethod(superclass, MethodReference.initSelector);
//                    superclass = superclass.getSuperclass().getSuperclass();
//                }
//                // this assertion should never fail, since it is garanteed that java.lang.Object
//                // has such a constructor
//                assert constructor != null;
//                addInvocation(ctx, new int[]{instance}, constructor.getReference(), IInvokeInstruction.Dispatch.SPECIAL);


                IMethod ctor = cha.resolveMethod(klass, MethodReference.initSelector);
                if (ctor != null) {
                    addInvocation(ctx, new int[]{instance}, ctor.getReference(), IInvokeInstruction.Dispatch.SPECIAL);
                } else {
                    IMethod superConstructor = klass.getMethod(MethodReference.initSelector);
                    if (superConstructor != null)
                        addInvocation(ctx, new int[]{instance}, superConstructor.getReference(), IInvokeInstruction.Dispatch.SPECIAL);
                }
            }
        }


        return result;
    }

    public int getValueNumberForIntConstant(Context c, int constant) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        Map<ConstantValue, Integer> constant2ValueNumber = triple.getMiddle();
        int nextLocal = triple.getRight();
        ConstantValue v = new ConstantValue(constant);
        Integer result = constant2ValueNumber.get(v);
        if (result == null) {
            result = nextLocal++;
            constant2ValueNumber.put(v, result);
        }

        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(triple.getLeft(), constant2ValueNumber, nextLocal));

        return result;
    }

    public int getNumberOfStatements(Context context) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = this.statementsPerContext.get(context);
        return triple == null ? 0 : triple.getLeft().size();
    }

//
//    public int getValueNumberForByteConstant(byte c) {
//        // treat it like an int constant for now.
//        ConstantValue v = new ConstantValue(c);
//        Integer result = constant2ValueNumber.get(v);
//        if (result == null) {
//            result = nextLocal++;
//            constant2ValueNumber.put(v, result);
//        }
//        return result;
//    }
//
//    public int getValueNumberForCharConstant(char c) {
//        // treat it like an int constant for now.
//        ConstantValue v = new ConstantValue(c);
//        Integer result = constant2ValueNumber.get(v);
//        if (result == null) {
//            result = nextLocal++;
//            constant2ValueNumber.put(v, result);
//        }
//        return result;
//    }

    public SSAPhiInstruction addPhi(Context c, List<Integer> values) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        Integer nextLocal = triple.getRight();

        int result = nextLocal++;
        int valArray[] = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            valArray[i] = values.get(i);
        }
        SSAPhiInstruction phi = instructionFactory.PhiInstruction(statements.size(), result, valArray);
        statements.add(phi);

        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(statements, triple.getMiddle(), nextLocal));
        return phi;
    }

    public int addGetInstance(Context c, FieldReference ref, int object) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        Integer nextLocal = triple.getRight();

        // add the instruction
        int result = nextLocal++;
        statements.add(instructionFactory.GetInstruction(statements.size(), result, object, ref));

        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(statements, triple.getMiddle(), nextLocal));

        return result;
    }

    public int addCheckcast(Context c, TypeReference[] types, int rv, boolean isPEI) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        int nextLocal = triple.getRight();
        int lv = nextLocal++;
        statements.add(instructionFactory.CheckCastInstruction(statements.size(), lv, rv, types, isPEI));
        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(statements, triple.getMiddle(), nextLocal));
        return lv;
    }

    public int addArrayLoad(Context c, final TypeReference elementType, final int baseObject, final int indexValue) {
        Triple<List<SSAInstruction>, Map<ConstantValue, Integer>, Integer> triple = statementsPerContext.getOrDefault(c, new ImmutableTriple<>(new ArrayList<>(), new HashMap<>(), initialNextLocal));
        List<SSAInstruction> statements = triple.getLeft();
        int nextLocal = triple.getRight();
        int lv = nextLocal++;

        statements.add(instructionFactory.ArrayLoadInstruction(statements.size(), lv, baseObject, indexValue, elementType));
        // update the inner cache
        statementsPerContext.put(c, new ImmutableTriple<>(statements, triple.getMiddle(), nextLocal));
        return lv;

    }

//    public int addGetStatic(FieldReference ref) {
//        int result = nextLocal++;
//        statements.add(instructionFactory.GetInstruction(statements.size(), result, ref));
//        return result;
//    }

    /**
     * A warning for when we fail to allocate a type in the fake root method
     */
    private static class AllocationFailure extends Warning {

        final TypeReference t;

        AllocationFailure(TypeReference t) {
            super(Warning.SEVERE);
            this.t = t;
        }

        public static AllocationFailure create(TypeReference t) {
            return new AllocationFailure(t);
        }

        @Override
        public String getMsg() {
            return getClass().toString() + " : " + t;
        }
    }

//    public void addSetInstance(final FieldReference ref, final int baseObject, final int value) {
//        statements.add(instructionFactory.PutInstruction(statements.size(), baseObject, value, ref));
//    }
//
//    public void addSetStatic(final FieldReference ref, final int value) {
//        statements.add(instructionFactory.PutInstruction(statements.size(), value, ref));
//    }
//
//    public void addSetArrayField(
//            final TypeReference elementType,
//            final int baseObject,
//            final int indexValue,
//            final int value) {
//        statements.add(
//                instructionFactory.ArrayStoreInstruction(statements.size(), baseObject, indexValue, value, elementType));
//    }
//
//    public int addGetArrayField(
//            final TypeReference elementType, final int baseObject, final int indexValue) {
//        int result = nextLocal++;
//        statements.add(
//                instructionFactory.ArrayLoadInstruction(statements.size(), result, baseObject, indexValue, elementType));
//        return result;
//    }
//
//    public RTAContextInterpreter getInterpreter() {
//        return new RTAContextInterpreter() {
//
//            @Override
//            public Iterator<NewSiteReference> iterateNewSites(CGNode node) {
//                ArrayList<NewSiteReference> result = new ArrayList<>();
//                SSAInstruction[] statements = getStatements(options.getSSAOptions());
//                for (SSAInstruction statement : statements) {
//                    if (statement instanceof SSANewInstruction) {
//                        SSANewInstruction s = (SSANewInstruction) statement;
//                        result.add(s.getNewSite());
//                    }
//                }
//                return result.iterator();
//            }
//
//            public Iterator<SSAInstruction> getInvokeStatements() {
//                ArrayList<SSAInstruction> result = new ArrayList<>();
//                SSAInstruction[] statements = getStatements(options.getSSAOptions());
//                for (SSAInstruction statement : statements) {
//                    if (statement instanceof SSAInvokeInstruction) {
//                        result.add(statement);
//                    }
//                }
//                return result.iterator();
//            }
//
//            @Override
//            public Iterator<CallSiteReference> iterateCallSites(CGNode node) {
//                final Iterator<SSAInstruction> I = getInvokeStatements();
//                return new Iterator<CallSiteReference>() {
//                    @Override
//                    public boolean hasNext() {
//                        return I.hasNext();
//                    }
//
//                    @Override
//                    public CallSiteReference next() {
//                        SSAInvokeInstruction s = (SSAInvokeInstruction) I.next();
//                        return s.getCallSite();
//                    }
//
//                    @Override
//                    public void remove() {
//                        Assertions.UNREACHABLE();
//                    }
//                };
//            }
//
//            @Override
//            public boolean understands(CGNode node) {
//                return node.getMethod()
//                        .getDeclaringClass()
//                        .equals(MethodModel.this.getDeclaringClass());
//            }
//
//            @Override
//            public boolean recordFactoryType(CGNode node, IClass klass) {
//                // not a factory type
//                return false;
//            }
//
//            @Override
//            public Iterator<FieldReference> iterateFieldsRead(CGNode node) {
//                return EmptyIterator.instance();
//            }
//
//            @Override
//            public Iterator<FieldReference> iterateFieldsWritten(CGNode node) {
//                return EmptyIterator.instance();
//            }
//        };
//    }
}
