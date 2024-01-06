package edu.rit.se.design.dodo.utils.graphs;

import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

/**
 * Utilities for testing the path finder classes.
 *
 * @author Joanna C. S. Santos - jds5109@rit.edu
 */
public class PathFinderTestUtils {
    /**
     * Creates a numbered graph from a string.
     * Ex.: ABCD
     * A->B; C->D.
     *
     * @param edges
     * @return
     */
    public static NumberedGraph<String> createGraph(String edges) {
        NumberedGraph<String> g = SlowSparseNumberedGraph.make();
        for (int i = 0; i < edges.length(); i += 2) {
            String from = edges.substring(i, i + 1);
            if (!g.containsNode(from)) {
                g.addNode(from);
            }

            String to = edges.substring(i + 1, i + 2);
            if (!g.containsNode(to)) {
                g.addNode(to);
            }

            g.addEdge(from, to);
        }
        return g;
    }

}
