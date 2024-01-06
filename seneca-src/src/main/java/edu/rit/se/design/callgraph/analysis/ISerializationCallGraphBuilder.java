package edu.rit.se.design.callgraph.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import edu.rit.se.design.callgraph.util.NameUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

public interface ISerializationCallGraphBuilder {
    /**
     * @return serialization synthetic methods in the worklist to be processed
     */
    Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> getSerializationWorkList();

    /**
     * @return deserialization synthetic methods in the worklist to be processed
     */
    Set<Triple<CGNode, SSAAbstractInvokeInstruction, CGNode>> getDeserializationWorkList();

    /**
     * Returns all the magic methods in the call graph (i.e., whose signature is in {@link NameUtils#ALL_MAGIC_METHODS}).
     *
     * @return a set of all magic methods in the built call graph.
     */
    Set<CGNode> getMagicMethods();
}
