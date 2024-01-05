package edu.rit.se.design.dodo.utils.viz;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.types.*;
import com.ibm.wala.util.strings.Atom;
import edu.rit.se.design.dodo.utils.TestUtils;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static com.ibm.wala.types.MethodReference.findOrCreate;
import static com.ibm.wala.types.TypeReference.findOrCreate;
import static org.junit.Assert.assertTrue;

/**
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class CFGVisualizerTest {

    //FIXME: change the hardcoded path to a relative path to the test resources
    private static final String TEST_ASSETS_FOLDER = TestUtils.ROOT_TEST_ASSETS + "dodo-utils/graphs/";
    private static final String OUTPUT_FOLDER = "./target/test-results/";

    @Test
    public void testGenerateVisualGraph() throws Exception {
        System.out.println("generateVisualGraph");
        SDG<InstanceKey> sdg = TestUtils.extractSDG(TEST_ASSETS_FOLDER + "Sample1.jar");
        System.out.println(sdg.getCallGraph().getEntrypointNodes().iterator().next());
        String className = "LSample1";
        String methodName = "execute";
        String methodDesc = "(Ljava/lang/String;III)Z";
        MethodReference mRef = getMethodReference(className, methodName, methodDesc);
        Set<CGNode> nodes = sdg.getCallGraph().getNodes(mRef);

        assertTrue("No node found", nodes.size() > 0);

        CGNode node = nodes.iterator().next();
        File dotFile = new File(OUTPUT_FOLDER + className + "-Simple.dot");
        CFGVisualizer instance = new CFGVisualizer(node, false);
        instance.generateVisualGraph(dotFile);
        System.out.println("File saved in " + dotFile.getAbsolutePath());
        assertTrue(dotFile.exists());
    }


    @Test
    public void testGenerateVisualClusteredGraph() throws Exception {
        System.out.println("generateVisualGraph");
        SDG<InstanceKey> sdg = TestUtils.extractSDG(TEST_ASSETS_FOLDER + "Sample1.jar");
        System.out.println(sdg.getCallGraph().getEntrypointNodes().iterator().next());
        String className = "LSample1";
        String methodName = "execute";
        String methodDesc = "(Ljava/lang/String;III)Z";
        MethodReference mRef = getMethodReference(className, methodName, methodDesc);
        Set<CGNode> nodes = sdg.getCallGraph().getNodes(mRef);

        assertTrue("No node found", nodes.size() > 0);

        CGNode node = nodes.iterator().next();
        File dotFile = new File(OUTPUT_FOLDER + className + "-Clustered.dot");
        CFGVisualizer instance = new CFGVisualizer(node, false);
        instance.generateVisualClusteredGraph(dotFile);
        System.out.println("File saved in " + dotFile.getAbsolutePath());
        assertTrue(dotFile.exists());
    }

    private static MethodReference getMethodReference(String className, String methodName, String methodDesc) {
        Atom methodNameAtom = Atom.findOrCreateUnicodeAtom(methodName);
        Descriptor executeDesc = Descriptor.findOrCreateUTF8(Language.JAVA, methodDesc);
        TypeName typeName = TypeName.string2TypeName(className);
        TypeReference typeRef = findOrCreate(ClassLoaderReference.Application, typeName);
        MethodReference mRef = findOrCreate(typeRef, methodNameAtom, executeDesc);
        return mRef;
    }


}
