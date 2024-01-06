package edu.rit.se.design.dodo.utils.wala;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallString;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallStringContext;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallStringContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.cfa.CallerSiteContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.*;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.collections.IteratorUtil;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.*;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.graph.traverse.BFSIterator;
import com.ibm.wala.util.graph.traverse.DFSAllPathsFinder;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetUtil;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Utility class for manipulating WALA's data structures.
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class WalaUtils {


    /**
     * It attempts to get the associated line number to a {@link NormalStatement}
     *
     * @param s the statement to get the line number
     * @return the line number (if possible); null in case that information couldn't be extracted.
     */
    public static Integer getLineNumber(NormalStatement s) {
        if (s == null) {
            throw new IllegalArgumentException(WalaUtils.class.getCanonicalName() + ":getLineNumber(NormalStatement s): param s can't be null");
        }

        try {
            int instructionIndex = s.getInstructionIndex();
            if (s.getNode().getMethod() instanceof ShrikeBTMethod) {
                int bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
                return s.getNode().getMethod().getLineNumber(bcIndex);
            }
            if (s.getNode().getMethod() instanceof JavaSourceLoaderImpl.ConcreteJavaMethod) {
                return ((JavaSourceLoaderImpl.ConcreteJavaMethod) s.getNode().getMethod()).getLineNumber(instructionIndex);
            }
        } catch (InvalidClassFileException ex) {
            Logger.getLogger(WalaUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Prune the CallGraph by removing non-application nodes.
     *
     * @param cg call graph
     * @return a subgraph of cg containing only "Application" nodes
     */
    public static Graph<CGNode> pruneCallGraph(CallGraph cg) {

        return GraphSlicer.prune(cg, new Predicate<CGNode>() {
            @Override
            public boolean test(CGNode n) {
                return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
            }
        });
    }

    /**
     * Given an {@link IClass} c, it returns a set with all the classes above it in the class hierarchy.
     *
     * @param c                 subject class
     * @param includeInterfaces true if the analysis shall include interfaces
     * @return a set with all the classes above c in the class hierarchy.
     */
    public static Set<IClass> getSuperClasses(IClass c, boolean includeInterfaces) {
        if (c == null) {
            throw new IllegalArgumentException("IClass c parameter can't be null");
        }

        Queue<IClass> toVisit = new LinkedList<>();
        Set<IClass> results = new HashSet<>();
        toVisit.add(c);
        while (!toVisit.isEmpty()) {
            IClass top = toVisit.poll();
            // adds superclass
            if (top.getSuperclass() != null) {
                results.add(top.getSuperclass());
                toVisit.add(top.getSuperclass());
            }
            // adds any interface
            if (includeInterfaces) {
                top.getAllImplementedInterfaces().forEach(classInterface -> {
                    results.add(classInterface);
                    toVisit.add(classInterface);
                });
            }
        }
        return results;
    }

    /**
     * @param sdg
     * @param stmt
     * @param filter
     * @return
     */
    public static SSAAbstractInvokeInstruction findClosestConstructor(SDG<InstanceKey> sdg, NormalStatement stmt, Set<String> filter) {
        if (stmt == null) {
            throw new IllegalArgumentException("SSAAbstractInvokeInstruction instruction parameter can't be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("Set<String> filter parameter can't be null");
        }
        //TODO implement

        //        return null;
        throw new UnsupportedOperationException("WALAUtils.findClosestConstructor(sdg,stmt,filter) is not implemented yet");
    }

    /**
     * Given an {@link IClass} c, it returns the first class high up in the hierarchy that matches any string from a given set of strings.
     *
     * @param c                 subject class
     * @param filter            a list of string to filter out the (super)classes of interest
     * @param includeInterfaces true if the analysis shall include interfaces
     * @return a set with all the classes above c in the class hierarchy.
     */
    public static IClass getClosestSuperclass(IClass c, boolean includeInterfaces, Set<String> filter) {
        if (c == null) {
            throw new IllegalArgumentException("IClass c parameter can't be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("Set<String> filter parameter can't be null");
        }

        Queue<IClass> toVisit = new LinkedList<>();
        toVisit.add(c);
        while (!toVisit.isEmpty()) {
            IClass top = toVisit.poll();
            // adds superclass
            if (top.getSuperclass() != null && filter.contains(top.getSuperclass().getName().toString())) {
                return top.getSuperclass();
            }
            // adds any interface
            if (includeInterfaces) {
                for (IClass classInterface : top.getAllImplementedInterfaces()) {
                    if (filter.contains(classInterface.getName().toString())) {
                        return classInterface;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds a node in the call graph from a specific class that matches a given method name. It searches for such node via a breadth-first
     * search
     *
     * @param cg
     * @param declaringClass fully-qualified class name (e.g., "Ljava/lang/Thread")
     * @param methodName     the name of the method (e.g., "toString")
     * @return
     */
    public static CGNode findNode(CallGraph cg, String declaringClass, String methodName) {
        BFSIterator<CGNode> bfsIterator = new BFSIterator<>(cg);

        while (bfsIterator.hasNext()) {
            CGNode cgNode = bfsIterator.next();

            if (isFromClass(cgNode, declaringClass) && isMethod(cgNode, methodName)) {
                return cgNode;
            }
        }
        throw new IllegalArgumentException("Node not found");
    }

    /**
     * Returns true if the cgNode matches the methodName
     *
     * @param cgNode     subject node
     * @param methodName name of the method
     * @return true if the cgNode matches the methodName
     */
    private static boolean isMethod(CGNode cgNode, String methodName) {
        return cgNode.getMethod().getName().toString().equals(methodName);
    }

    /**
     * Verifies whether the provided call graph node is from a class of interest.
     *
     * @param cgNode
     * @param declaringClass
     * @return
     */
    private static boolean isFromClass(CGNode cgNode, String declaringClass) {
        return cgNode.getMethod().getDeclaringClass().getName().toString().equals(declaringClass);
    }

    /**
     * True if the IClass is under the application-scope ({@code ClassLoaderReference.Application}).
     *
     * @param iClass
     * @return
     */
    public static boolean isApplicationScope(IClass iClass) {
        return iClass != null && iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application);
    }

    /**
     * True if the IClass is under the extension-scope ({@code ClassLoaderReference.Extension}).
     *
     * @param iClass
     * @return
     */
    public static boolean isExtensionScope(IClass iClass) {
        return iClass != null && iClass.getClassLoader().getReference().equals(ClassLoaderReference.Extension);
    }

    /**
     * True if the CGNode is under the application-scope ({@code ClassLoaderReference.Application}).
     *
     * @param node call graph node.
     * @return true if the node is declared in the application.
     */
    public static boolean isApplicationScope(CGNode node) {
        return node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
    }


    /**
     * True if the CGNode is under the application-scope ({@code ClassLoaderReference.Application}).
     *
     * @param node call graph node.
     * @return true if the node is declared in the application.
     */
    public static boolean isExtensionScope(CGNode node) {
        return node.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Extension);
    }

    /**
     * True if the statement is under the application-scope.
     *
     * @param s
     * @return
     */
    public static boolean isApplicationScope(Statement s) {
        return s.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
    }

    /**
     * True if the statement is under Java-scope (primordial)
     *
     * @param s
     * @return
     */
    public static boolean isPrimordialScope(Statement s) {
        return s.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial);
    }

    /**
     * True if the class is under Java-scope (primordial)
     *
     * @param c
     * @return
     */
    public static boolean isPrimordialScope(IClass c) {
        return c.getClassLoader().getReference().equals(ClassLoaderReference.Primordial);
    }

    /**
     * True if the call graph node is under Java-scope (primordial)
     *
     * @param cgNode
     * @return
     */
    public static boolean isPrimordialScope(CGNode cgNode) {
        return cgNode.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial);
    }

    /**
     * It checks whether the target of the invoke instruction (i.e., the callee) is within the application scope.
     *
     * @param invokeInst
     * @return
     */
    public static boolean isTargetApplicationScope(SSAInvokeInstruction invokeInst) {
        return invokeInst.getDeclaredTarget().getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application);
    }

    /**
     * Checks whether instruction is Normal (i.e., it is not Phi, Pi nor GetCaught).
     *
     * @param instruction the instruction to be checked
     * @return false if it Phi, Pi or GetCaught. True otherwise.
     */
    public static boolean isNormalInstruction(SSAInstruction instruction) {
        return !(instruction instanceof SSAPhiInstruction || instruction instanceof SSAPiInstruction || instruction instanceof SSAGetCaughtExceptionInstruction);
    }

    /**
     * Returns the root statement for a passed in SDG.
     *
     * @param sdg the system dependence graph for a program.
     * @return the root statement for a passed in SDG.
     */
    public static Statement getRootStatement(SDG<InstanceKey> sdg) {
        return new BFSIterator<>(sdg).next();
    }
//    public static Pair<JREVersion, J2EEVersion> getJavaVersion(String jarPath) throws IOException {
//
//        JarFile jarFile = new JarFile(jarPath);
//        Enumeration<JarEntry> entries = jarFile.entries();
//        while (entries.hasMoreElements()) {
//            JarEntry entry = entries.nextElement();
//
//            String entryName = entry.getName();
//            if (entryName.endsWith(".class")) {
//                InputStream in = jarFile.getInputStream(entry);
//                ClassReader reader = new ClassReader(in);
//                ClassNode classNode = new ClassNode();
//                reader.accept(classNode, 0);
//
//                // The minor version is stored in the 16 most significant bits
//                // the major version in the 16 least significant bits.
//                int version = classNode.version;
//
//                switch (0x0000FFFF & version) {
//                    //        47 = Java 1.3
//                    case 47:
//                        return new ImmutablePair<>(JREVersion.JRE1_3, J2EEVersion.J2EE6);
//                    //        48 = Java 1.4
//                    case 48:
//                        return new ImmutablePair<>(JREVersion.JRE1_4, J2EEVersion.J2EE6);
//                    //        49 = Java 5
//                    case 49:
//                        return new ImmutablePair<>(JREVersion.JRE1_5, J2EEVersion.J2EE6);
//
//                    //        50 = Java 6
//                    case 50:
//                        return new ImmutablePair<>(JREVersion.JRE1_6, J2EEVersion.J2EE6);
//                    //        51 = Java 7
//                    case 51:
//                        return new ImmutablePair<>(JREVersion.JRE1_7, J2EEVersion.J2EE7);
//                    //        52 = Java 8
//                    case 52:
//                        return new ImmutablePair<>(JREVersion.JRE1_8, J2EEVersion.J2EE8);
//                }
//            }
//        }
//        return new ImmutablePair<>(JREVersion.JRE1_8, J2EEVersion.J2EE8);// default fallback is V8
//    }

    private static Statement findStatement(SSAInstruction defInst, PDG pdg) {
        Iterator<Statement> predNodesIterator = pdg.iterator();
        while (predNodesIterator.hasNext()) {
            Statement stmt = predNodesIterator.next();

            if (stmt instanceof NormalStatement && ((NormalStatement) stmt).getInstructionIndex() == defInst.iIndex()) {
                return stmt;
            }
            if (stmt instanceof PhiStatement && ((PhiStatement) stmt).getPhi().iIndex() == defInst.iIndex()) {
                return stmt;
            }
        }
        assert false;
        return null;
    }

    private static void addParamCallersToQueue(SDG sdg, Statement currentStmt, PDG currentPDG, int currentVarNo, Queue<Pair<Integer, Statement>> toVisit, Map<CGNode, Set<Integer>> visitedVarDefs) {
        sdg.getPredNodes(currentStmt).forEachRemaining(s -> {

            // is data dependency
            if (!currentPDG.isControlDependend((Statement) s, currentStmt)) {
                if (s instanceof ParamCallee && ((ParamCallee) s).getValueNumber() == currentVarNo) {
                    ParamCallee pc = (ParamCallee) s;
                    assert sdg.getPredNodeCount(s) == 1;
                    ParamCaller paramCaller = (ParamCaller) sdg.getPredNodes(s).next();

                    int paramCallerVarNo = paramCaller.getValueNumber();

                    PDG pdgParamCaller = sdg.getPDG(currentStmt.getNode());
                    Iterator<Statement> predNodesIterator = sdg.getPredNodes(paramCaller);
                    while (predNodesIterator.hasNext()) {
                        Statement predStmt = predNodesIterator.next();
                        if (!pdgParamCaller.isControlDependend((Statement) predStmt, paramCaller)) {
                            if (visitedVarDefs.containsKey(predStmt.getNode()) && visitedVarDefs.get(predStmt.getNode()).contains(paramCallerVarNo)) {
                                continue;
                            }
                            toVisit.add(new ImmutablePair<>(paramCallerVarNo, predStmt));
                        }
                    }

                }
            }

        });
    }

    private static void addPhiToQueue(Statement currentStmt, Queue<Pair<Integer, Statement>> toVisit, Map<CGNode, Set<Integer>> visitedVarDefs) {
        PhiStatement phiStmt = (PhiStatement) currentStmt;
        CGNode phiNode = phiStmt.getNode();
        SSAPhiInstruction phiInstruction = phiStmt.getPhi();
        for (int i = 0; i < phiInstruction.getNumberOfUses(); i++) {
            if (visitedVarDefs.containsKey(phiNode) && visitedVarDefs.get(phiNode).contains(phiInstruction.getUse(i))) {
                continue;
            }
            toVisit.add(new ImmutablePair<>(phiInstruction.getUse(i), currentStmt));
        }

    }

    /**
     * Finds where a given variable has been defined in the code.
     *
     * @param sdg      System Dependence Graph
     * @param stmt     the statement of interest
     * @param useIndex the use index of interest (recall that uses starts from 0 and goes all the way up to instruction.getNumberOfUses()).
     *                 We assume you will pass a correct useIndex (i.e., that callee wont trigger an Out of Bounds Exception)
     * @return a list of statements that define the variable of interest
     * @throws IllegalArgumentException
     * @throws CancelException
     */
    public static List<Statement> getDefinitionForUsingPa(SDG<InstanceKey> sdg, NormalStatement stmt, int useIndex) throws IllegalArgumentException, CancelException {
        return getDefinitionForUsingPa(sdg.getCallGraph(), sdg.getPointerAnalysis(), stmt.getNode(), stmt.getInstruction(), useIndex);
    }

    /**
     * Finds where a given variable has been defined in the code.
     *
     * @param cg          Callgraph
     * @param pa          pointer analysis object (generated during callgraph construction)
     * @param cgNode      callgraph node that contains the instruction of interest
     * @param instruction the instruction of interest
     * @param useIndex    the use index of interest (recall that uses starts from 0 and goes all the way up to instruction.getNumberOfUses()).
     *                    We assume you will pass a correct useIndex (i.e., that callee wont trigger an Out of Bounds Exception)
     * @return a list of statements that define the variable of interest
     * @throws IllegalArgumentException
     * @throws CancelException
     */
    public static List<Statement> getDefinitionForUsingPa(CallGraph cg, PointerAnalysis<InstanceKey> pa, CGNode cgNode, SSAInstruction instruction, int useIndex) throws IllegalArgumentException, CancelException {
        List<Statement> results = new ArrayList();

        int varNo = instruction.getUse(useIndex);
        PointerKey pointerKeyForLocal = pa.getHeapModel().getPointerKeyForLocal(cgNode, varNo);

        OrdinalSet<InstanceKey> pointsToSet = pa.getPointsToSet(pointerKeyForLocal);

        pointsToSet.forEach((InstanceKey pointsTo) -> {
            Iterator<com.ibm.wala.util.collections.Pair<CGNode, NewSiteReference>> creationSites = pointsTo.getCreationSites(cg);
            creationSites.forEachRemaining((com.ibm.wala.util.collections.Pair<CGNode, NewSiteReference> pair) -> {
                NewSiteReference site = pair.snd;
                CGNode node = pair.fst;
                int newInstructionIndex = node.getIR().getNewInstructionIndex(site);
                results.add(new NormalStatement(node, newInstructionIndex));
            });

        });

        return results;
    }

    public static List<Statement> getDefinitionFor(SDG sdg, NormalStatement stmt, int useIndex) throws IllegalArgumentException, CancelException {
        int varNo = stmt.getInstruction().getUse(useIndex);

        List<Statement> results = new ArrayList();
        Map<CGNode, Set<Integer>> visitedVarDefs = new HashMap();
        Queue<Pair<Integer, Statement>> toVisit = new LinkedList<>();

        toVisit.add(new ImmutablePair<>(varNo, stmt));

        while (!toVisit.isEmpty()) {
            Pair<Integer, Statement> top = toVisit.poll();
            Statement currentStmt = top.getRight();
            int currentVarNo = top.getLeft();
            PDG currentPDG = sdg.getPDG(currentStmt.getNode());
            CGNode currentCgNode = currentStmt.getNode();
            DefUse currentDU = currentCgNode.getDU();
            SSAInstruction defInstr = currentDU.getDef(currentVarNo);
            SymbolTable symTable = currentCgNode.getIR() != null ? currentCgNode.getIR().getSymbolTable() : null;

            System.out.println("Def for " + currentVarNo);
            System.out.print("\t");
            System.out.println(defInstr);
            System.out.println(defInstr instanceof SSANewInstruction);

            if (defInstr instanceof SSANewInstruction) {
                results.add(findStatement(defInstr, currentPDG));
            }
            if (defInstr instanceof SSAPhiInstruction) {
                addPhiToQueue(findStatement(defInstr, currentPDG), toVisit, visitedVarDefs);
            }

            if (symTable != null) {
                if (symTable.isConstant(currentVarNo)) {
                    throw new IllegalArgumentException("This method is used only for finding object definitions interprocedurally");
                }
                if (symTable.isParameter(currentVarNo)) {
                    addParamCallersToQueue(sdg, currentStmt, currentPDG, currentVarNo, toVisit, visitedVarDefs);
                }
            }

            if (!visitedVarDefs.containsKey(currentCgNode)) {
                visitedVarDefs.put(currentCgNode, new HashSet<>());
            }
            visitedVarDefs.get(currentCgNode).add(currentVarNo);

        }

        return results;
    }

    /**
     * Convert an IClass's name to a fully qualified name (e.g. java.lang.String).
     *
     * @param c the {@link IClass} object.
     * @return the class' fully qualified name.
     */
    public static String getFullyQualifiedName(IClass c) {
        String className = c.getName().toString();
        return getFullyQualifiedName(className);
    }
    /**
     * Convert an IClass's name to a fully qualified name (e.g. java.lang.String).
     *
     * @param className the class name (ex: Ljava/lang/String)
     * @return the class' fully qualified name.
     */
    public static String getFullyQualifiedName(String className) {
        return className.substring(1).replace("/", ".");
    }
    /**
     * Return a view of an {@link IClassHierarchy} as a {@link Graph}, with edges from classes to immediate subtypes
     *
     * @param cha
     * @return
     */
    public static Graph<IClass> typeHierarchy2Graph(IClassHierarchy cha) {
        Graph<IClass> result = SlowSparseNumberedGraph.make();
        for (IClass c : cha) {
            result.addNode(c);
        }
        for (IClass c : cha) {
            cha.getImmediateSubclasses(c).forEach((x) -> {
                result.addEdge(c, x);
            });
            if (c.isInterface()) {
                cha.getImplementors(c.getReference()).forEach((x) -> {
                    result.addEdge(c, x);
                });
            }
        }
        return result;
    }

    public static List<List<ISSABasicBlock>> findPathToMethodEntry(CGNode cgNode, SSAInstruction instruction) {
        IR ir = cgNode.getIR();
        SSACFG cfg = ir.getControlFlowGraph();
        ISSABasicBlock basicBlockForInstruction = ir.getBasicBlockForInstruction(instruction);

        DFSAllPathsFinder<ISSABasicBlock> finder = new DFSAllPathsFinder<ISSABasicBlock>(cfg, cfg.entry(), new Predicate<ISSABasicBlock>() {
            @Override
            public boolean test(ISSABasicBlock s) {
                return basicBlockForInstruction.equals(s);
            }
        });
        List<List<ISSABasicBlock>> paths = new ArrayList<>();
        List<ISSABasicBlock> path;
        while ((path = finder.find()) != null) {
            Collections.reverse(path);
            paths.add(path);
        }

        return paths;
    }

    /**
     * Returns an {@link IClass} given a type in the format: "L/java...".
     * The search is at this order: Application, Extension, Primordial.
     *
     * @param classname class name (in JVM format L/package/to/Class)
     * @return the found {@IClass} or null
     */
    public static IClass findClass(IClassHierarchy cha, String classname) {
        for (IClassLoader loader : cha.getLoaders()) {
            IClass iClass = cha.lookupClass(TypeReference.findOrCreate(loader.getReference(), classname));
            if (iClass != null) return iClass;
        }
        return null;
    }

    public static void printObjectAsJson(Object obj, int numTabs, Set<Object> visitedObj) throws IllegalArgumentException, IllegalAccessException {
        if (obj == null) return;
        visitedObj.add(obj);
        String smallIndent = StringUtils.repeat("\t", numTabs);
        String indent = StringUtils.repeat("\t", numTabs + 1);

        System.out.println("{");
        System.out.println(format("%s\"__INSTANCE__\":%s@%s,", indent, obj.getClass().getName(), Integer.toHexString(obj.hashCode())));

        for (Field f : obj.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;

            f.setAccessible(true);
            Object objectVal = f.get(obj);

            if (!f.getType().isPrimitive() && !f.getType().equals(String.class)) {
                if (objectVal == null)
                    System.out.println(format("%s\"%s %s\":null,", indent, Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName(), f.getName(), objectVal));
                else if (!visitedObj.contains(objectVal)) {
                    System.out.print(format("%s\"%s %s\":", indent, Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName(), f.getName()));
                    printObjectAsJson(objectVal, numTabs + 1, visitedObj);
                }
            } else {
                if (objectVal instanceof String) objectVal = "\"" + objectVal.toString() + "\"";
                System.out.println(format("%s\"%s %s\":%s,", indent, Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName(), f.getName(), objectVal));
            }
        }
        System.out.println(smallIndent + "}");
    }

    /**
     * Verifies whether the node has an instruction that calls a method of interest.
     *
     * @param n         callgraph node
     * @param methodRef a method reference
     * @return list of all places that invokes the methodRef.
     */
    public static Collection<SSAInvokeInstruction> findSSAInvokeInstructions(CGNode n, MethodReference methodRef) {
        Collection<SSAInvokeInstruction> result = HashSetFactory.make();
        IR ir = n.getIR();
        if (ir != null) {
            for (SSAInstruction s : Iterator2Iterable.make(ir.iterateAllInstructions())) {
                if (s instanceof SSAInvokeInstruction) {
                    SSAInvokeInstruction call = (SSAInvokeInstruction) s;
                    if (call.getCallSite().getDeclaredTarget().equals(methodRef)) {
                        IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
                        Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
                        result.add(call);
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Verifies whether the node has an instruction that calls a method of interest.
     *
     * @param n
     * @param methodRef
     * @return
     */
    public static Collection<Statement> findCallTo(CGNode n, MethodReference methodRef) {
        Collection<Statement> result = HashSetFactory.make();
        IR ir = n.getIR();
        if (ir != null) {
            findSSAInvokeInstructions(n, methodRef).forEach(call -> {
                IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
                result.add(new NormalStatement(n, indices.intIterator().next()));
            });
            return result;
        }
        return null;
    }

    public static String contextToString(Context context) {
        if (context instanceof Everywhere) return "Context: Ø";
        if (context instanceof CallerSiteContext) {
            CallerSiteContext callerSiteContext = (CallerSiteContext) context;
            return "CallSiteContext: N" + callerSiteContext.getCaller().getGraphNodeId() + " @ " + callerSiteContext.getCallSite().getProgramCounter();
        }
        if (context instanceof CallStringContext) {
            CallString callString = (CallString) ((CallStringContext) context).get(CallStringContextSelector.CALL_STRING);
            StringBuilder str = new StringBuilder("[");
            CallSiteReference[] sites = callString.getCallSiteRefs();
            IMethod[] methods = callString.getMethods();
            for (int i = 0; i < sites.length; i++) {
                str.append(' ')
                        .append(WalaUtils.methodToString(methods[i].getReference()))
                        .append('@')
                        .append(sites[i].getProgramCounter());
            }
            str.append(" ]");
            return "CallStringContext: " + str;
        }

        return context.toString();
    }

    /**
     * Converts a MethodReference to a more concise string.
     *
     * @param declaredTarget method reference for a target method call
     * @return a concise (cleaner) string for it
     */
    public static String methodToString(MethodReference declaredTarget) {
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < declaredTarget.getNumberOfParameters(); i++) {
            TypeReference parameterType = declaredTarget.getParameterType(i);
            if (parameterType.isPrimitiveType()) {
                if (parameterType.equals(TypeReference.Int))
                    parameters.append("int");
                else if (parameterType.equals(TypeReference.Float))
                    parameters.append("float");
                else if (parameterType.equals(TypeReference.Boolean))
                    parameters.append("bool");
                else if (parameterType.equals(TypeReference.Short))
                    parameters.append("short");
                else if (parameterType.equals(TypeReference.Void))
                    parameters.append("void");
                else if (parameterType.equals(TypeReference.Double))
                    parameters.append("double");
                else if (parameterType.equals(TypeReference.Char))
                    parameters.append("char");
                else parameters.append(parameterType.getName().getClassName());
            } else if (parameterType.isClassType())
                parameters.append(parameterType.getName().getClassName());
            else if (parameterType.isArrayType())
                parameters.append(parameterType.getInnermostElementType().getName().getClassName())
                        .append(StringUtils.repeat("[]", parameterType.getDimensionality()));
            if (i < declaredTarget.getNumberOfParameters() - 1)
                parameters.append(",");
        }

        String classname = declaredTarget.getDeclaringClass().getName().getClassName().toString();
        String methodName = declaredTarget.getName().toString();

        return format("%s.%s(%s)", classname, methodName, parameters);
    }


    /**
     * Prune a numbered graph to only the nodes accepted by the {@link Predicate} p
     *
     * @param g   graph to be trimmed
     * @param p   the criteria to *keep* a node
     * @param <T> the type of nodes in the graph
     * @return a trimmed graph.
     */
    public static <T> NumberedGraph<T> prune(final NumberedGraph<T> g, final Predicate<T> p) {
        if (g == null) {
            throw new IllegalArgumentException("g is null");
        }
        final NumberedNodeManager<T> n =
                new NumberedNodeManager<T>() {
                    @Override
                    public int getNumber(T N) {
                        if (this.containsNode(N)) return g.getNumber(N);
                        else return -1;
                    }

                    @Override
                    public T getNode(int number) {
                        T N = g.getNode(number);
                        if (this.containsNode(N)) return N;
                        else throw new NoSuchElementException();
                    }

                    @Override
                    public int getMaxNumber() {
                        int max = -1;
                        for (T N : g) {
                            if (containsNode(N) && getNumber(N) > max) {
                                max = getNumber(N);
                            }
                        }

                        return max;
                    }

                    /**
                     * @param s
                     * @return iterator of nodes with the numbers in set s
                     */
                    @Override
                    public Iterator<T> iterateNodes(IntSet s) {
                        return new FilterIterator<>(g.iterateNodes(s), p);
                    }

                    int nodeCount = -1;

                    @Override
                    public Iterator<T> iterator() {
                        return new FilterIterator<>(g.iterator(), p);
                    }

                    @Override
                    public Stream<T> stream() {
                        return g.stream().filter(p);
                    }

                    @Override
                    public int getNumberOfNodes() {
                        if (nodeCount == -1) {
                            nodeCount = IteratorUtil.count(iterator());
                        }
                        return nodeCount;
                    }

                    @Override
                    public void addNode(T n) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeNode(T n) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public boolean containsNode(T n) {
                        return p.test(n) && g.containsNode(n);
                    }
                };
        final NumberedEdgeManager<T> e =
                new NumberedEdgeManager<T>() {

                    /**
                     * @param N
                     * @return the numbers identifying the immediate successors of node
                     */
                    @Override
                    public IntSet getSuccNodeNumbers(T N) {
                        MutableIntSet bits = IntSetUtil.make();
                        for (T EE : Iterator2Iterable.make(getSuccNodes(N))) {
                            bits.add(g.getNumber(EE));
                        }

                        return bits;
                    }

                    /**
                     * @param node
                     * @return the numbers identifying the immediate predecessors of node
                     */
                    @Override
                    public IntSet getPredNodeNumbers(T node) {
                        MutableIntSet bits = IntSetUtil.make();
                        for (T EE : Iterator2Iterable.make(getPredNodes(node))) {
                            bits.add(g.getNumber(EE));
                        }

                        return bits;
                    }

                    @Override
                    public Iterator<T> getPredNodes(T n) {
                        return new FilterIterator<>(g.getPredNodes(n), p);
                    }

                    @Override
                    public int getPredNodeCount(T n) {
                        return IteratorUtil.count(getPredNodes(n));
                    }

                    @Override
                    public Iterator<T> getSuccNodes(T n) {
                        return new FilterIterator<>(g.getSuccNodes(n), p);
                    }

                    @Override
                    public int getSuccNodeCount(T N) {
                        return IteratorUtil.count(getSuccNodes(N));
                    }

                    @Override
                    public void addEdge(T src, T dst) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeEdge(T src, T dst) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeAllIncidentEdges(T node) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeIncomingEdges(T node) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeOutgoingEdges(T node) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public boolean hasEdge(T src, T dst) {
                        return g.hasEdge(src, dst) && p.test(src) && p.test(dst);
                    }
                };
        AbstractNumberedGraph<T> output =
                new AbstractNumberedGraph<T>() {

                    @Override
                    protected NumberedNodeManager<T> getNodeManager() {
                        return n;
                    }

                    @Override
                    protected NumberedEdgeManager<T> getEdgeManager() {
                        return e;
                    }
                };

        return output;
    }
}
