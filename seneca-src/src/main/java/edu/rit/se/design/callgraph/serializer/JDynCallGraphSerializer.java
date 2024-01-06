package edu.rit.se.design.callgraph.serializer;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static edu.rit.se.design.callgraph.serializer.JavaCallGraphSerializer.method2String;
import static java.lang.String.format;

/**
 * Serializes a call graph into a text format.
 * Each line has the following elements: from_node, to_node, from_scope, to_scope
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class JDynCallGraphSerializer implements ICallGraphSerializer {
    /**
     * Saves a file in a given format.
     *
     * @param cg         call graph
     * @param outputFile where to save the file
     */
    @Override
    public void save(CallGraph cg, File outputFile) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (CGNode cgNode : cg) {
                IMethod method = cgNode.getMethod();
                cg.getSuccNodes(cgNode).forEachRemaining(target -> {
                    stringBuilder.append(
                            format("%s %s %s %s\n",
                                    method2String(method),
                                    method2String(target.getMethod()),
                                    method.getDeclaringClass().getClassLoader().getReference().getName().toString(),
                                    target.getMethod().getDeclaringClass().getClassLoader().getReference().getName().toString()
                            )
                    );
                });
            }

            FileUtils.write(outputFile, stringBuilder.toString(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
