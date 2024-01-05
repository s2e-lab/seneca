package edu.rit.se.design.dodo.utils.graphs;

import java.util.*;

/**
 * This class represents the backbone of a trie structure. Intended to organize Statements/Nodes from a callgraph into
 * a representation of all possible paths from a given root.
 * @author Brandon Greet - brandon.greet@mail.rit.edu
 */
public class TrieNode<T> implements Iterable<TrieNode<T>> {
    private final TrieNode<T> parent;
    private List<TrieNode<T>> children;
    private final T data;
    private final int height;

    /**
     * @param parent    Parent node in trie (null indicates this is root)
     * @param data      Object to be organized in trie
     * @param height    Denotes layer in trie
     */
    TrieNode(TrieNode<T> parent, T data, int height) {
        if (data == null)
            throw new IllegalArgumentException("Data cannot be null.");
        this.parent = parent;
        this.data = data;
        this.height = height;
        this.children = new ArrayList<>();
    }

    public TrieNode<T> getParent() {
        return this.parent;
    }

    public T getData() {
        return this.data;
    }

    public int getHeight() {
        return this.height;
    }

    public void addChild(TrieNode<T> child) {
        this.children.add(child);
    }

    @Override
    public Iterator<TrieNode<T>> iterator() {
        return this.children.iterator();
    }

    /**
     * Populate a list with objects organized in the trie from root to this node.
     * @param b An empty list
     */
    public void getBranch(List<T> b) {
        if (!b.isEmpty())
            throw new IllegalArgumentException("List parameter b must be empty.");

        // Checks that this node isn't root
        if (this.parent != null)
            this.parent.getBranch(b);

        b.add(this.data);
    }
}
