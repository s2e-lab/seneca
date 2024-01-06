package edu.rit.se.design.dodo.utils.graphs;

import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.graph.NumberedGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>This class finds all paths from source to destination nodes in graphs (cyclical or acyclical).
 * We traverse the graph in a breadth-first fashion.</p>
 * <p>
 * Implementation is based on the class {@link com.ibm.wala.util.graph.traverse.BFSPathFinder}.
 * </p>
 *
 * @author Joanna C. S. Santos - jds5109@rit.edu.
 */
public class BFSAllPathsFinder<T> {

    // numbered graph to perform the search
    private final NumberedGraph<T> graph;

    // predicate that indicates whether a node is a destination or not
    private final Predicate<T> filter;

    // start nodes of the search
    private final Iterator<T> roots;

    /**
     * @param g      numbered graph to perform the search
     * @param filter it indicates whether a node is a destination or not
     * @param root   source of the search
     */
    public BFSAllPathsFinder(NumberedGraph<T> g, Predicate<T> filter, T root) {
        if (g == null || filter == null || root == null) {
            throw new IllegalArgumentException("All parameters should not be null");
        }

        this.graph = g;
        this.filter = filter;
        this.roots = new NonNullSingletonIterator<>(root);
    }


    /**
     * @param g      numbered graph to perform the search
     * @param filter it indicates whether a node is a destination or not
     * @param roots  sources of the search
     */
    public BFSAllPathsFinder(NumberedGraph<T> g, Predicate<T> filter, Iterator<T> roots) {
        if (g == null || filter == null || roots == null) {
            throw new IllegalArgumentException("All parameters should not be null");
        }

        this.graph = g;
        this.filter = filter;
        this.roots = roots;
    }


    /**
     * @return list of all paths found; null if nothing was found
     */
    public List<List<T>> find() {
        // holds all the found paths
        List<List<T>> foundPaths = new ArrayList<>();

        // queue which also stores paths
        Queue<List<T>> queue = new LinkedList<>();

        // initialize queue with paths that are 1-sized (with root node only)
        roots.forEachRemaining(root -> queue.add(Arrays.asList(root)));

        while (!queue.isEmpty()) {
            List<T> currentPath = queue.poll();
            Set<Integer> visitedInPath = currentPath.parallelStream().map(n -> graph.getNumber(n)).collect(Collectors.toSet());
            T lastNode = currentPath.get(currentPath.size() - 1);
            // if the last node in the current path is a destination, adds path to the results
            if (filter.test(lastNode)) {
                foundPaths.add(currentPath);
            }

            Iterator<T> succNodes = graph.getSuccNodes(lastNode);
            while (succNodes.hasNext()) {
                T successor = succNodes.next();
                // node hasn't been visited yet in current path
                if (!visitedInPath.contains(graph.getNumber(successor))) {
                    // create a new path from earlier path and append this vertex
                    List<T> newPath = new ArrayList<>(currentPath);
                    newPath.add(successor);

                    // insert new path to queue
                    queue.add(newPath);
                }
            }

        }

        return foundPaths;
    }
}
