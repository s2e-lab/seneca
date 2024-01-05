package edu.rit.se.design.dodo.utils.viz;

import com.ibm.wala.ipa.slicer.Statement;

import java.util.Set;

/**
 * Highlights nodes in green if they are part of the slice.
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class StatementNodeHighlighter implements GraphVisualizer.NodeHighlighter<Statement> {

    private final Set<Statement> slice;

    public StatementNodeHighlighter(Set<Statement> slice) {
        this.slice = slice;
    }

    @Override
    public String getAttributes(Statement s) {
        return slice.contains(s) ? "[fillcolor=palegreen,color=darkseagreen]" : "[fillcolor=white,color=black]";

    }
}
