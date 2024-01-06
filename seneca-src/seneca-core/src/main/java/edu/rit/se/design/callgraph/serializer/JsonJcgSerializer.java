package edu.rit.se.design.callgraph.serializer;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Serializes a class using the format described in the paper by Reif et al.
 *
 * @author Joanna C. S. Santos
 */
public class JsonJcgSerializer implements ICallGraphSerializer {


    private static String toJVMString(TypeReference typeReference) {
        if (typeReference.isClassType() || isArrayOfClassType(typeReference)) {
            return typeReference.getName().toString() + ";";
        } else {
            return typeReference.getName().toString();
        }
    }

    private static boolean isArrayOfClassType(TypeReference typeReference) {
        if (typeReference.isArrayType()) {
            TypeReference elementType = typeReference.getArrayElementType();
            if (elementType.isClassType()) {
                return true;
            } else {
                return isArrayOfClassType(elementType);
            }
        } else {
            return false;
        }
    }


    // Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])
    private static JsonObject methodToJson(MethodReference method) {
        JsonObject jsonObject = new JsonObject();


        String name = method.getName().toString();
        String declaringClass = toJVMString(method.getDeclaringClass());
        String returnType = toJVMString(method.getReturnType());
        JsonArray parameterTypes = new JsonArray();
        for (int i = 0; i < method.getNumberOfParameters(); i++) {
            parameterTypes.add(toJVMString(method.getParameterType(i)));
        }

        jsonObject.addProperty("name", name);
        jsonObject.addProperty("declaringClass", declaringClass);
        jsonObject.addProperty("returnType", returnType);
        jsonObject.addProperty("parameterTypes", returnType);

        return jsonObject;
    }

    //    CallSite(declaredTarget: Method, line: Int, pc: Option[Int], targets: Set[Method])
    private static JsonObject callsiteToJson(CallGraph cg, CGNode cgNode, CallSiteReference cs) {
        JsonObject jsonObject = new JsonObject();
        MethodReference declaredTarget = cs.getDeclaredTarget();
        int pc = cs.getProgramCounter();
        int lineno = -1;
        try {
            lineno = cgNode.getMethod().getLineNumber(pc);
        } catch (ArrayIndexOutOfBoundsException e) {
            lineno = -1;
        }
        jsonObject.add("declaredTarget", methodToJson(declaredTarget));
        jsonObject.addProperty("line", lineno);
        jsonObject.addProperty("pc", pc);

        JsonArray targets = new JsonArray();
        for (CGNode possibleTarget : cg.getPossibleTargets(cgNode, cs)) {
            targets.add(methodToJson(possibleTarget.getMethod().getReference()));
        }


        jsonObject.add("targets", targets);


        return jsonObject;
    }


    /**
     * Saves a file in a given format.
     *
     * @param cg         call graph
     * @param outputFile where to save the file
     */
    @Override
    public void save(CallGraph cg, File outputFile) {

        Queue<CGNode> worklist = new LinkedList(cg.getEntrypointNodes());
        Set<CGNode> processed = new HashSet<CGNode>();

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonObject results = new JsonObject();
        JsonArray reachableMethods = new JsonArray();

        // FORMAT:
        //    ReachableMethod(method: Method, callSites: Set[CallSite])
        //    CallSite(declaredTarget: Method, line: Int, pc: Option[Int], targets: Set[Method])
        //    Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])
        while (!worklist.isEmpty()) {
            CGNode cgNode = worklist.poll();
            processed.add(cgNode);
            Iterator<CallSiteReference> callSiteReferenceIterator = cgNode.iterateCallSites();
            callSiteReferenceIterator.forEachRemaining(csr -> {
                Set<CGNode> possibleTargets = cg.getPossibleTargets(cgNode, csr);
                for (CGNode possibleTarget : possibleTargets) {
                    if (!processed.contains(possibleTarget)) {
                        worklist.add(possibleTarget);
                    }
                }
            });

        }


        results.add("reachableMethods", reachableMethods);
        // saves to file
        try (Writer writer = new FileWriter(outputFile)) {
            gson.toJson(results, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
