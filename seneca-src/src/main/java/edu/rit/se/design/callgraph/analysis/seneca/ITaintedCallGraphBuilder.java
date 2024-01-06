package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import edu.rit.se.design.callgraph.analysis.ISerializationCallGraphBuilder;

import java.util.Set;

/**
 * Common interface for taint-based call graph construction algorithms.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public interface ITaintedCallGraphBuilder extends ISerializationCallGraphBuilder {

    /**
     * Obtains the computed tainted pointers.
     *
     * @return the set of tainted pointers.
     */
    Set<PointerKey> getTaintedPointers();

    /**
     * What nodes in the call graph are tainted
     *
     * @return a set of tainted call graph nodes.
     */
    Set<CGNode> getTaintedNodes();

    /**
     * @param node call graph node
     * @return True if node is tainted
     */
    boolean isTainted(CGNode node);

    /**
     * Marks a call graph node as tainted
     *
     * @param node call graph node
     * @return the node's taint state
     */
    void setTainted(CGNode node);

    /**
     * Checks the taint state of a pointer
     *
     * @param pk pointer for a variable
     * @return true if pointer is tainted
     */
    boolean isTainted(PointerKey pk);

    /**
     * Marks  a pointer as tainted
     *
     * @param pk pointer for a variable
     */
    void setTainted(PointerKey pk);
}
