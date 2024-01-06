package edu.rit.se.design.dodo.utils.viz;




import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.PDG;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Statement;

import static com.ibm.wala.ipa.slicer.Statement.Kind.METHOD_ENTRY;
import static com.ibm.wala.ipa.slicer.Statement.Kind.NORMAL;

/**
 *
 * @author Joanna C. S. Santos <jds5109@rit.edu>
 */
public class StatementEdgeHighlighter implements GraphVisualizer.EdgeHighlighter<Statement> {

    private final SDG sdg;

    // colors
    private static final String BLACK = "black";
    private static final String RED = "0.002 0.999 0.999";
    private static final String GREEN = "0.259 0.525 0.957";

    public StatementEdgeHighlighter(SDG sdg) {
        if (sdg == null) {
            throw new IllegalArgumentException("sdg parameter cant be null");
        }
        this.sdg = sdg;
    }

    @Override
    public String getAttributes(Statement from, Statement to) {

        String format = "color=\"%s\",style=%s";
        CGNode fromNode = from.getNode();
        CGNode toNode = to.getNode();

        if (fromNode.equals(toNode)) {
            PDG pdg = sdg.getPDG(fromNode);

            if (pdg.isControlDependend(from, to)) {
                // control edges are in dashed red arrows
                return String.format(format, RED, "dashed");
            } else if (!pdg.hasEdge(from, to)) {
                // fake edges are in bolded  green
                return String.format(format, GREEN, "bold");
            }
        }else{
            if(from.getKind() == NORMAL && to.getKind() == METHOD_ENTRY)
                return String.format(format, RED, "dashed"); // control edge
        }
        return String.format(format, BLACK, "solid");
    }

}
