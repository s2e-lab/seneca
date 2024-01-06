package edu.rit.se.design.callgraph.serializer;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import edu.rit.se.design.dodo.utils.viz.GraphVisualizer;

import java.io.File;

/**
 * Saves a call graph in dot format.
 *
 * @author Joanna C. S. Santos
 */
public class DotCallGraphSerializer implements ICallGraphSerializer {
    /**
     * Saves a file in DOT format.
     *
     * @param cg         call graph
     * @param outputFile where to save the file
     */
    @Override
    public void save(CallGraph cg, File outputFile) {
        new GraphVisualizer<CGNode>("Call graph",
                GraphVisualizer.getDefaultCgNodeLabeller(),
                GraphVisualizer.getDefaultCgNodeHighlighter(),
                null,
                GraphVisualizer.getDefaultCgNodeRemover(cg))
                .generateVisualGraph(cg, outputFile);
    }
}
