package edu.rit.se.design.callgraph.evaluation.utils;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.*;
import com.ibm.wala.util.graph.traverse.DFSAllPathsFinder;
import com.ibm.wala.util.strings.Atom;
import org.junit.Assert;

import java.util.List;
import java.util.Set;

public class TestUtilities {

    //    public static final String TC_ROOT_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/fse-2021-serialization/dataset/build/";
    public static final String TC_ROOT_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/joannacss/seneca-scripts/dataset/build/";
    public static final String CVES_ROOT_FOLDER = TC_ROOT_FOLDER + "../vulnerabilities/";
    public static final String DRIVER_CLASS = CVES_ROOT_FOLDER + "Driver.jar";


    public static final String DEFAULT_EXCLUSIONS_FILE = "exclusions+regex.txt";//"exclusions.txt";
    public static final String EXCLUSIONS_FILE = DEFAULT_EXCLUSIONS_FILE;
    public static final String EXCLUSIONS_FILE_XERCES = "exclusions+regex-xerces.txt";

    //    public static final String ICSE_2023_STATIC_CGS_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/icse-2023-seneca/static-cgs";
//    public static final String FSE_2021_STATIC_CGS_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/fse-2021-serialization/static-cgs";
//    public static final String SOAP_2021_STATIC_CGS_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/soap-2021-paper/static-cgs";
    public static final String STATIC_CGS_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/joannacss/seneca-scripts/static-cgs";
    public static final String VULN_PATHS_FOLDER = System.getProperty("user.home") + "/Documents/Portfolio/GitHub/joannacss/seneca-scripts/results/rq4";

    //    public static final String ICSE_2023_CATS_STATIC_CGS_FOLDER = ICSE_2023_STATIC_CGS_FOLDER + "/cats";
//    public static final String FSE_2021_CATS_STATIC_CGS_FOLDER = FSE_2021_STATIC_CGS_FOLDER + "/cats";
//    public static final String SOAP_2021_CATS_STATIC_CGS_FOLDER = SOAP_2021_STATIC_CGS_FOLDER + "/cats";
    public static final String CATS_STATIC_CGS_FOLDER = STATIC_CGS_FOLDER + "/cats";

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
     * @param cg
     * @param from
     * @param to
     */
    public static void checkDirectCall(CallGraph cg, MethodReference from, MethodReference to) {
        Set<CGNode> fromNodes = cg.getNodes(from);
        Set<CGNode> toNodes = cg.getNodes(to);
        Assert.assertTrue("Missing " + from.getSignature() + " from call graph", fromNodes.size() != 0);
        Assert.assertTrue("Missing " + to.getSignature() + " from call graph", toNodes.size() != 0);
        Assert.assertTrue(cg.hasEdge(fromNodes.iterator().next(), toNodes.iterator().next()));
    }

}
