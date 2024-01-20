package oopsla.evaluation.utils;


import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.*;
import com.ibm.wala.util.graph.traverse.DFSAllPathsFinder;
import com.ibm.wala.util.strings.Atom;

import java.util.List;
import java.util.Set;

/**
 * Utility class for testing.
 *
 * @author Joanna C. S. Santos jds5109@rit.edu
 */
public class EvaluationUtil {


    // ============== Input Properties ==============

    // Program Property: -Dtestcase_folder=/path/to/testcases (e.g., -Dtestcase_folder=/Users/joanna/Documents/Portfolio/GitHub/joannacss/seneca-scripts/dataset/build/)
    public static final String TC_ROOT_FOLDER = System.getProperty("testcase_folder");


    // ============== Output Properties ==============
    // Program Property: -Dstatic_cgs_folder=/path/to/where/static/call/graphs/should/be/saved (e.g., -Dstatic_cgs_folder=/Users/joanna/Documents/Portfolio/GitHub/joannacss/seneca-scripts/static-cgs)
    public static final String STATIC_CGS_FOLDER = System.getProperty("static_cgs_folder");
    // Program Property: -Dvuln_paths_folder=/path/to/where/vulnerable/paths/should/be/saved (e.g., -Dvuln_paths_folder=/Users/joanna/Documents/Portfolio/GitHub/joannacss/seneca-scripts/results/rq4)
    public static final String VULN_PATHS_FOLDER = System.getProperty("vuln_paths_folder");


    // ============== Derived Paths from Properties ==============
    // where the CVE JARs are stored
    public static final String CVES_ROOT_FOLDER = TC_ROOT_FOLDER + "../vulnerabilities/";
    // the driver class for the CVEs
    public static final String DRIVER_CLASS = CVES_ROOT_FOLDER + "Driver.jar";
    // the folder where the compiled JAR files for the CATS test case programs are stored
    public static final String CATS_STATIC_CGS_FOLDER = STATIC_CGS_FOLDER + "/cats";


    // ============== CG Construction Configuration ==============
    // default exclusion file for the call graph construction (default one)
    public static final String DEFAULT_EXCLUSIONS_FILE = "exclusions+regex.txt";//"exclusions.txt";
    // exclusion file actually used across all experiments
    public static final String EXCLUSIONS_FILE = DEFAULT_EXCLUSIONS_FILE;
    // exclusion file for the call graph construction (for the Xerces project, which needed a different one)
    public static final String EXCLUSIONS_FILE_XERCES = "exclusions+regex-xerces.txt";


    /**
     * Finds all paths from a given method to another method in the call graph.
     *
     * @param cg   call graph
     * @param from method reference of the starting method
     * @param to   method reference of the ending method (target method)
     * @return number of paths found
     */
    public static int findAllPath(CallGraph cg, MethodReference from, MethodReference to) {
        CGNode fromNode = cg.getNodes(from).iterator().next();
        DFSAllPathsFinder<CGNode> finder = new DFSAllPathsFinder<>(cg, fromNode, node -> node.getMethod().getReference().equals(to));
        int count = 0;
        List<CGNode> path;
        while ((path = finder.find()) != null) {
            System.err.println(path);
            count++;
        }
        return count;
    }

    /**
     * Creates a method reference object needed for performing testing if a given method is in the call graph.
     *
     * @param cl         class loader (application, extension, primordial)
     * @param className  class name in bytecode format (i.e., Ljava/lang/String)
     * @param methodName method name
     * @param descriptor descriptor in bytecode format (e.g. (Ljava/lang/String)V)
     * @return a {@link MethodReference}
     */
    public static MethodReference createMethodRef(ClassLoaderReference cl, String className, String methodName, String descriptor) {
        TypeName typeName = TypeName.string2TypeName(className);
        TypeReference typeRef = TypeReference.findOrCreate(cl, typeName);
        Atom mn = Atom.findOrCreateUnicodeAtom(methodName);
        Descriptor md = Descriptor.findOrCreateUTF8(descriptor);
        return MethodReference.findOrCreate(typeRef, mn, md);
    }

    /**
     * Asserts that there is a direct call from node n to a node x.
     *
     * @param cg   call graph
     * @param from method reference of the starting method
     * @param to   method reference of the ending method (target method)
     * @throws RuntimeException if there is no direct call from n to x or if n or x are not in the call graph.
     */
    public static void checkDirectCall(CallGraph cg, MethodReference from, MethodReference to) {
        Set<CGNode> fromNodes = cg.getNodes(from);
        Set<CGNode> toNodes = cg.getNodes(to);

        if (fromNodes.size() == 0)
            throw new RuntimeException("Missing " + from.getSignature() + " from call graph");
        if (toNodes.size() == 0)
            throw new RuntimeException("Missing " + to.getSignature() + " from call graph");
        if (!cg.hasEdge(fromNodes.iterator().next(), toNodes.iterator().next()))
            throw new RuntimeException("Missing edge from " + from.getSignature() + " to " + to.getSignature());
    }


}
