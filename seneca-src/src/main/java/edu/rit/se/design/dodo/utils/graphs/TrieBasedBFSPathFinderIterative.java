package edu.rit.se.design.dodo.utils.graphs;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.util.graph.NumberedGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class offers memory optimization by representing all possible paths from a source as a trie structure.
 * Its path finding is based on {@link AllPathsFinderIterative} and {@link com.ibm.wala.util.graph.traverse.BFSPathFinder}
 *
 * @author Brandon Greet - brandon.greet@mail.rit.edu
 */
public class TrieBasedBFSPathFinderIterative<T> implements IPathFinderIterative {
    // Maximum height of the trie/length of any path
    private final Integer pathSizeThreshold;
    // Graph to search through
    private final NumberedGraph<T> graph;
    // Condition that indicates if a node is an intended destination for a path
    private final Predicate<T> filter;
    // Lists the sources to search from
    private final Iterator<T> roots;
    // Root of the trie currently being processed
    private TrieNode<T> currentRoot;
    // Holds current leaf nodes in trie for breadth-first algorithm
    private Queue<TrieNode<T>> queue;

    /**
     * @param g                     Numbered graph to search through
     * @param p                     Condition to detect an intended destination
     * @param roots                 List sources to search from
     * @param pathSizeThreshold     Max size of any possible path to find
     */
    public TrieBasedBFSPathFinderIterative(NumberedGraph<T> g, Predicate<T> p, Iterator<T> roots, Integer pathSizeThreshold) {
        if (g == null)
            throw new IllegalArgumentException("Graph cannot be null.");
        if (p == null)
            throw new IllegalArgumentException("Predicate cannot be null");
        if (roots == null || !roots.hasNext())
            throw new IllegalArgumentException("Roots cannot be null or empty.");

        this.graph = g;
        this.filter = p;
        this.roots = roots;
        this.pathSizeThreshold = pathSizeThreshold;
        this.queue = new LinkedList<>();

        //Plant first trie
        TrieNode<T> firstRoot = new TrieNode<>(null, roots.next(), 0);
        this.currentRoot = firstRoot;
        this.queue.add(firstRoot);
    }


    Set<T> slice ;
    public void setSlice(Set<T> slice){
        this.slice = slice;
    }

    /**
     * Each call attempts to find a path while building out the current trie.
     * @return A list of nodes representing a path
     */
    @Override
    public List<T> find() {
        // While there is a leaf node to be processed
        while (!this.queue.isEmpty()) {
            TrieNode<T> currentLeaf = this.queue.poll();

            // Ignore children of synthetic root nodes
            if (isSyntheticFakeRootMethod(currentLeaf.getData())) continue;

            // If currentLeaf is a destination, return the branch
            if (this.filter.test(currentLeaf.getData())) {
                List<T> branch = new ArrayList<>();
                currentLeaf.getBranch(branch);
                return branch;
            }

            // Check if current trie is complete
            if (this.pathSizeThreshold == null || currentLeaf.getHeight() < this.pathSizeThreshold-1) {
                // Trie is not complete, construct next layer
                List<T> branch = new ArrayList<>();
                currentLeaf.getBranch(branch);
                Set<Integer> visitedInPath = branch.parallelStream().map(n -> this.graph.getNumber(n)).collect(Collectors.toSet());

                Iterator<T> successors = this.graph.getSuccNodes(currentLeaf.getData());
                while (successors.hasNext()) {
                    T successor = successors.next();
                    // Ignore nodes not in the slice
                    if (this.slice != null && !this.slice.contains(successor)) continue;
                    if (!visitedInPath.contains(this.graph.getNumber(successor))) {
                        TrieNode<T> newLeaf = new TrieNode<>(currentLeaf, successor, currentLeaf.getHeight() + 1);
                        currentLeaf.addChild(newLeaf);
                        this.queue.add(newLeaf);
                    }
                }
            }
            if (this.queue.isEmpty() && this.roots.hasNext()) {
                // Trie is complete and final node has been processed
                // Make a new trie
                TrieNode<T> newRoot = new TrieNode<>(null, this.roots.next(), 0);
                this.currentRoot = newRoot;
                this.queue.add(newRoot);
            }
        }

        return null;
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
