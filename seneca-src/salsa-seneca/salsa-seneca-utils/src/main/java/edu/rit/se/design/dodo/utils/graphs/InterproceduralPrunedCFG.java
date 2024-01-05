package edu.rit.se.design.dodo.utils.graphs;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.AbstractInterproceduralCFG;
import com.ibm.wala.ipa.cfg.ExceptionPrunedCFG;
import com.ibm.wala.ipa.cfg.PrunedCFG;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import java.util.function.Predicate;
/**
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class InterproceduralPrunedCFG extends AbstractInterproceduralCFG<ISSABasicBlock> {
    public InterproceduralPrunedCFG(CallGraph CG) {
        super(CG);
    }

    public InterproceduralPrunedCFG(CallGraph cg, Predicate<CGNode> filtersection) {
        super(cg, filtersection);
    }

    /**
     * @return the cfg for n, or null if none found
     * @throws IllegalArgumentException if n == null
     */
    @Override
    public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getCFG(CGNode n)
            throws IllegalArgumentException {
        if (n == null) {
            throw new IllegalArgumentException("n == null");
        }
        if (n.getIR() == null) {
            return null;
        }
        ControlFlowGraph<SSAInstruction, ISSABasicBlock> cfg = n.getIR().getControlFlowGraph();
        if (cfg == null) {
            return null;
        }





        return ExceptionPrunedCFG.make(cfg);
    }
}
