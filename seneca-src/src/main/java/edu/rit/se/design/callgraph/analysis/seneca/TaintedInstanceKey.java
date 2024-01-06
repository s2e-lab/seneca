package edu.rit.se.design.callgraph.analysis.seneca;

import com.ibm.wala.analysis.reflection.InstanceKeyWithNode;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;

import java.util.Iterator;
import java.util.Objects;

import static edu.rit.se.design.dodo.utils.wala.WalaUtils.methodToString;

/**
 * It represents an inexistent instantiation.
 * This is used to instantiate tainted pointers for creating suitable call graphs for deserialization analysis.
 * Joanna C. S. Santos (jds5109@rit.edu)
 */
public class TaintedInstanceKey implements InstanceKeyWithNode {

    private IClass serializableType;
    private CGNode node;

    public TaintedInstanceKey(IClass serializableType, CGNode node) {
        this.serializableType = serializableType;
        this.node = node;
    }

    /**
     * @return the node which created this instance.
     */
    @Override
    public CGNode getNode() {
        return this.node;
    }

    /**
     * For now, we assert that each InstanceKey represents a set of classes which are all of the same
     * concrete type (modulo the fact that all arrays of references are considered concrete type
     * []Object;)
     */
    @Override
    public IClass getConcreteType() {
        return this.serializableType;
    }

    /**
     * Get the creation sites of {@code this}, i.e., the statements that may allocate objects
     * represented by {@code this}. A creation site is a pair (n,s), where n is the containing {@link
     * CGNode} in the given {@link CallGraph} {@code CG} and s is the allocating {@link
     * NewSiteReference}.
     *
     * @param CG
     */
    @Override
    public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
        Assertions.UNREACHABLE("You shouldn't be calling this because this variable had no previous allocation (synthetic)");
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaintedInstanceKey that = (TaintedInstanceKey) o;
        return Objects.equals(serializableType, that.serializableType) &&
                Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serializableType, node);
    }


    @Override
    public String toString() {
        return "TaintedInstanceKey{" +
                "serializableType=" + serializableType.getName() +
                ", node=" + node.getGraphNodeId() + " " + methodToString(node.getMethod().getReference()) +
                '}';
    }
}
