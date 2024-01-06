package edu.rit.se.design.dodo.utils.graphs;


import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.graph.NumberedGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class finds all paths from source to destination nodes in graphs (it can have cycles).
 * We traverse the graph in a breadth- or depth-first fashion.
 * Each call to the <code>find()</code> method attempts to find another path.
 * <p>
 * Implementation is based on the class {@link com.ibm.wala.util.graph.traverse.BFSPathFinder}.
 *
 * @author Joanna C. S. Santos - jds5109@rit.edu.
 */
public class AllPathsFinderIterative<T> implements IPathFinderIterative {

    public enum SearchStrategy {
        BFS, DFS
    }

    // threshold for the size of the path
    private Integer pathSizeThreshold;
    // numbered graph to perform the search
    private final NumberedGraph<T> graph;
    // predicate that indicates whether a node is a destination or not
    private final Predicate<T> filter;
    // start nodes of the search
    private final Iterator<T> roots;

    // queue that stores paths
    private Queue<List<T>> queue;
    // stack that stores paths
    private Stack<List<T>> stack;
    // breadth- or depth-first search
    private SearchStrategy strategy;


    /**
     * BFS will be used by default
     *
     * @param g         numbered graph to perform the search
     * @param predicate it indicates whether a node is a destination or not
     * @param root      starting node of the search
     */
    public AllPathsFinderIterative(NumberedGraph<T> g, Predicate<T> predicate, T root) {
        this(null, g, predicate, new NonNullSingletonIterator<>(root), SearchStrategy.BFS);
    }

    /**
     * @param g         numbered graph to perform the search
     * @param predicate it indicates whether a node is a destination or not
     * @param roots     sources of the search
     * @param roots     starting nodes of the search
     */
    public AllPathsFinderIterative(NumberedGraph<T> g, Predicate<T> predicate, Iterator<T> roots) {
        this(null, g, predicate, roots, SearchStrategy.BFS);
    }


    /**
     * @param pathSizeThreshold the cut-off criteria to not explore paths whose length is higher than the threshold (null or a value <= 0 will disable the cut-off criteria and search for all paths in all possible lengths)
     * @param g                 numbered graph to perform the search
     * @param predicate         it indicates whether a node is a destination or not
     * @param roots             sources of the search
     */
    public AllPathsFinderIterative(Integer pathSizeThreshold, NumberedGraph<T> g, Predicate<T> predicate, Iterator<T> roots) {
        this(pathSizeThreshold, g, predicate, roots, SearchStrategy.BFS);
    }

    /**
     * @param pathSizeThreshold the cut-off criteria to not explore paths whose length is higher than the threshold (null or a value <= 0 will disable the cut-off criteria and search for all paths in all possible lengths)
     * @param g                 numbered graph to perform the search
     * @param predicate         it indicates whether a node is a destination or not
     * @param roots             sources of the search
     * @param strategy          search strategy (BFS = breadth-first; DFS = depth-first)
     */
    public AllPathsFinderIterative(Integer pathSizeThreshold, NumberedGraph<T> g, Predicate<T> predicate, Iterator<T> roots, SearchStrategy strategy) {
        if (g == null || predicate == null || roots == null)
            throw new IllegalArgumentException("All parameters should not be null");

        this.graph = g;
        this.filter = predicate;
        this.roots = roots;
        this.strategy = strategy;
        // initialize underlying data structure for traversal
        if (this.strategy == SearchStrategy.DFS) this.stack = new Stack<>();
        if (this.strategy == SearchStrategy.BFS) this.queue = new LinkedList<>();
        // initialize queue/stack with paths that are 1-sized (with root node only)
        this.roots.forEachRemaining(r -> addNewPath(Arrays.asList(r)));
        this.pathSizeThreshold = pathSizeThreshold != null && pathSizeThreshold > 0 ? pathSizeThreshold : null;
    }


    /**
     * Each call to this method attempts to find another path.
     *
     * @return a list of the current path found; null if nothing was found
     */
    @Override
    public List<T> find() {

        while (this.hasNext()) {
            List<T> currentPath = getNextPath();
            Set<Integer> visitedInPath = currentPath.parallelStream().map(n -> this.graph.getNumber(n)).collect(Collectors.toSet());
            T lastNode = currentPath.get(currentPath.size() - 1);

            // ignores the successors of the synthetic root nodes
            if (isSyntheticFakeRootMethod(lastNode)) continue;

            // If the last node is a destination, return the current path
            if (this.filter.test(lastNode)) return currentPath;

            // adds more paths to the queue to be expanded
            Iterator<T> successors = this.graph.getSuccNodes(lastNode);
            while (successors.hasNext()) {
                T successor = successors.next();
                // node hasn't been visited yet in current path
                if (!visitedInPath.contains(this.graph.getNumber(successor))) {
                    // create a new path from earlier path and append this vertex
                    List<T> newPath = new ArrayList<>(currentPath);
                    newPath.add(successor);
                    // insert new path to queue (if it is smaller than the current threshold - if defined)
                    if (pathSizeThreshold == null || newPath.size() <= pathSizeThreshold)
                        addNewPath(newPath);
                }
            }

            // if the last node in the current path is a destination, returns path
            //if (this.filter.test(lastNode)) return currentPath;

        }

        return null;
    }

    /**
     * Checks whether there are other paths to explore
     *
     * @return true if there are paths that still need to be explored
     */
    private boolean hasNext() {
        if (this.strategy == SearchStrategy.DFS)
            return !this.stack.isEmpty();
        else if (this.strategy == SearchStrategy.BFS)
            return !this.queue.isEmpty();
        else throw new IllegalStateException("Couldn't execute hasNext(). Only BFS or DFS strategies are supported");
    }

    /**
     * It adds a new path to be later visited
     *
     * @param newPath the path to be added to the stack/queue (DFS/BFS, respectively)
     */
    private void addNewPath(List<T> newPath) {
        if (this.strategy == SearchStrategy.DFS)
            this.stack.add(newPath);
        else if (this.strategy == SearchStrategy.BFS)
            this.queue.add(newPath);
        else throw new IllegalStateException("Couldn't add new path. Only BFS or DFS strategies are supported");
    }

    /**
     *
     * @return the next path in the stack/queue to be explored
     */
    private List<T> getNextPath() {
        if (this.strategy == SearchStrategy.DFS)
            return this.stack.pop();
        else if (this.strategy == SearchStrategy.BFS)
            return this.queue.poll();
        else throw new IllegalStateException("Couldn't retrieve next path. Only BFS or DFS strategies are supported");
    }

    /**
     * (Useful when graphs are control-flow graphs or call graphs. It has no other use)
     * @param lastNode checks whether the node is a fake root method
     * @return true in case the parameter is the fake root method
     */
    private boolean isSyntheticFakeRootMethod(T lastNode) {
        if (lastNode instanceof BasicBlockInContext && (((BasicBlockInContext) lastNode).getNode().getMethod() instanceof FakeRootMethod))
            return true;
        if (lastNode instanceof CGNode && (((CGNode) lastNode).getMethod() instanceof FakeRootMethod))
            return true;
        return false;
    }

}
