package edu.rit.se.design.dodo.utils.graphs;

import com.ibm.wala.cfg.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.collections.IteratorUtil;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.AbstractNumberedGraph;
import com.ibm.wala.util.graph.NumberedEdgeManager;
import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.graph.NumberedNodeManager;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetUtil;
import com.ibm.wala.util.intset.MutableIntSet;
import edu.rit.se.design.dodo.utils.wala.WalaUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Creates a view of an SDG in which edges are path-flows (instead of control-flow dependencies).
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class SDGPruner {

    private final SDG<InstanceKey> sdg;

    private final Set<Statement> slice;

    public SDGPruner(SDG<InstanceKey> sdg, Set<Statement> slice) {
        if (sdg == null) throw new IllegalArgumentException("sdg is null");
        if (slice == null) throw new IllegalArgumentException("slice is null");
        this.sdg = sdg;
        this.slice = slice;
    }


    /**
     * Prune an SDG such that:
     * (a) it maintains only the nodes in the slice provided in the constructor;
     * (b) edges are flow-paths
     *
     *
     * @return a trimmed graph.
     */
    public NumberedGraph<Statement> prune() {
        Predicate<Statement> filter = s -> slice.contains(s);

        final NumberedNodeManager<Statement> n =
                new NumberedNodeManager<Statement>() {
                    @Override
                    public int getNumber(Statement N) {
                        if (this.containsNode(N)) return sdg.getNumber(N);
                        else return -1;
                    }

                    @Override
                    public Statement getNode(int number) {
                        Statement N = sdg.getNode(number);
                        if (this.containsNode(N)) return N;
                        else throw new NoSuchElementException();
                    }

                    @Override
                    public int getMaxNumber() {
                        int max = -1;
                        for (Statement N : sdg) {
                            if (containsNode(N) && getNumber(N) > max) {
                                max = getNumber(N);
                            }
                        }

                        return max;
                    }

                    /**
                     * @param s
                     * @return iterator of nodes with the numbers in set s
                     */
                    @Override
                    public Iterator<Statement> iterateNodes(IntSet s) {
                        return new FilterIterator<>(sdg.iterateNodes(s), filter);
                    }

                    int nodeCount = -1;

                    @Override
                    public Iterator<Statement> iterator() {
                        return new FilterIterator<>(sdg.iterator(), filter);
                    }

                    @Override
                    public Stream<Statement> stream() {
                        return sdg.stream().filter(filter);
                    }

                    @Override
                    public int getNumberOfNodes() {
                        if (nodeCount == -1) {
                            nodeCount = IteratorUtil.count(iterator());
                        }
                        return nodeCount;
                    }

                    @Override
                    public void addNode(Statement n) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeNode(Statement n) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public boolean containsNode(Statement n) {
                        return filter.test(n) && sdg.containsNode(n);
                    }
                };
        final NumberedEdgeManager<Statement> e =
                new NumberedEdgeManager<Statement>() {

                    /**
                     * @param N
                     * @return the numbers identifying the immediate successors of node
                     */
                    @Override
                    public IntSet getSuccNodeNumbers(Statement N) {
                        MutableIntSet bits = IntSetUtil.make();
                        for (Statement EE : Iterator2Iterable.make(getSuccNodes(N))) {
                            bits.add(sdg.getNumber(EE));
                        }

                        return bits;
                    }

                    /**
                     * @param node
                     * @return the numbers identifying the immediate predecessors of node
                     */
                    @Override
                    public IntSet getPredNodeNumbers(Statement node) {
                        MutableIntSet bits = IntSetUtil.make();
                        for (Statement EE : Iterator2Iterable.make(getPredNodes(node))) {
                            bits.add(sdg.getNumber(EE));
                        }

                        return bits;
                    }

                    @Override
                    public Iterator<Statement> getPredNodes(Statement n) {
                        return new FilterIterator<>(sdg.getPredNodes(n), filter);
                    }

                    @Override
                    public int getPredNodeCount(Statement n) {
                        return IteratorUtil.count(getPredNodes(n));
                    }

                    @Override
                    public Iterator<Statement> getSuccNodes(Statement n) {
                        if(n instanceof StatementWithInstructionIndex){
                            SSAInstruction instruction = ((StatementWithInstructionIndex) n).getInstruction();
                            if(instruction instanceof SSAConditionalBranchInstruction){
//                                Util.e
                            }
                        }
                        return new FilterIterator<>(sdg.getSuccNodes(n), filter);
                    }

                    @Override
                    public int getSuccNodeCount(Statement N) {
                        return IteratorUtil.count(getSuccNodes(N));
                    }

                    @Override
                    public void addEdge(Statement src,Statement dst) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeEdge(Statement src, Statement dst) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeAllIncidentEdges(Statement node) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeIncomingEdges(Statement node) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public void removeOutgoingEdges(Statement node) {
                        Assertions.UNREACHABLE();
                    }

                    @Override
                    public boolean hasEdge(Statement src, Statement dst) {
                        return sdg.hasEdge(src, dst) && filter.test(src) && filter.test(dst);
                    }
                };
        AbstractNumberedGraph<Statement> output =
                new AbstractNumberedGraph<Statement>() {

                    @Override
                    protected NumberedNodeManager<Statement> getNodeManager() {
                        return n;
                    }

                    @Override
                    protected NumberedEdgeManager<Statement> getEdgeManager() {
                        return e;
                    }
                };

        return output;
    }


}
