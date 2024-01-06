package edu.rit.se.design.dodo.utils.viz;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.Util;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.ExceptionPrunedCFG;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.graph.traverse.BFSIterator;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * This is a utility class for visualizing Control Flow Graphs as a DOT file.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class CFGVisualizer {

    private final CGNode node;
    private final boolean usePrunedCFG;

    /**
     * @param node         call graph node that we want to visualize its CFG
     * @param usePrunedCFG if true, this flag uses the {@link ExceptionPrunedCFG} instead of
     *                     the default one returned by the node's IR (i.e., {@link IR#getControlFlowGraph()}).
     */
    public CFGVisualizer(CGNode node, boolean usePrunedCFG) {
        this.node = node;
        this.usePrunedCFG = usePrunedCFG;
    }

    /**
     * It plots a graph of the basic blocks and their inter-dependencies.
     *
     * @param dotFile where to save the generated dot file
     * @throws IOException if the dot file could not be written
     */
    public void generateVisualGraph(File dotFile) throws IOException {
        if (dotFile == null) throw new IllegalArgumentException("dotFile can't be null");


        ControlFlowGraph<SSAInstruction, ISSABasicBlock> cfg = getControlFlowGraph();

        StringBuilder dotStringBuffer = new StringBuilder();
        dotStringBuffer.append("digraph G {\n");
        dotStringBuffer.append("graph[label=\"").append(dotFile.getName()).append("\"];\n");
        dotStringBuffer.append("node[style=filled,fillcolor =\"white\",shape=box,margin=0.02,width=0,height=0];\n");

        for (int i = 0; i <= cfg.getNumber(cfg.exit()); i++) {
            ISSABasicBlock bb = cfg.getNode(i);
            String currentBB = format("BB%d", i);
            Iterator<ISSABasicBlock> succNodes = cfg.getSuccNodes(bb);
            while (succNodes.hasNext()) {
                String nextBB = format("BB%d", succNodes.next().getNumber());
                dotStringBuffer.append(format("%s -> %s", currentBB, nextBB)).append("\n");
            }
        }

        dotStringBuffer.append("}");

        try (FileWriter fw = new FileWriter(dotFile)) {
            fw.write(dotStringBuffer.toString());
        }
    }


    /**
     * Converts a CFG to a clustered format (i.e., it plots the blocks and the instructions within it).
     *
     * @param dotFile where to save the generated DOT file.
     * @throws IOException error on DOT file generation
     */
    public void generateVisualClusteredGraph(File dotFile) throws IOException {

        StringBuilder edgeStringBuffer = new StringBuilder();

        StringBuilder dotStringBuffer = new StringBuilder();
        dotStringBuffer.append("digraph G {\n");
        dotStringBuffer.append("\tgraph[label=\"").append(dotFile.getName()).append("\",compound=true];\n");
        dotStringBuffer.append("\tnode[style=filled,fillcolor =\"lightgrey\",shape=box,margin=0.02,width=0,height=0];\n");

        dotStringBuffer.append("\t// Blocks\n");


        IR ir = node.getIR();
        ControlFlowGraph<SSAInstruction, ISSABasicBlock> cfg = getControlFlowGraph();


        SymbolTable sTable = ir.getSymbolTable();
        BFSIterator<ISSABasicBlock> it = new BFSIterator<>(cfg);
        while (it.hasNext()) {
            ISSABasicBlock bb = it.next();
            int blockNo = bb.getNumber();
            List<SSAInstruction> allInstructions = ((SSACFG.BasicBlock) bb).getAllInstructions()
                    .stream()
                    .filter(instruction -> instruction != null)
                    .collect(Collectors.toList());
            StringBuilder s = new StringBuilder();
            String format = "\tsubgraph cluster_%d {\n\t\tcolor=blue;\n\t\tnode[style=filled];\n\t\tlabel=\"BB%d\";\n%s\n\t}\n";

            s.append("\t\t");
            if (allInstructions.isEmpty()) {
                s.append("BB").append(blockNo).append("[style=invis]");
            }

            for (int i = 0; i < allInstructions.size(); i++) {
                s.append("\"").append(getLabel(allInstructions.get(i), sTable)).append("\"");
                s.append(i < allInstructions.size() - 1 ? " -> " : "");
            }

            dotStringBuffer.append(format(format, blockNo, blockNo, s));

            int ifTrueBlockNo = -1, ifFalseBlockNo = -1;
            if (bb.getLastInstructionIndex() >= 0 && Util.endsWithConditionalBranch(cfg, bb)) {
                ifTrueBlockNo = Util.getTakenSuccessor(cfg, bb).getNumber();
                ifFalseBlockNo = Util.getNotTakenSuccessor(cfg, bb).getNumber();
            }

            Iterator<ISSABasicBlock> succNodes = cfg.getSuccNodes(bb);
            while (succNodes.hasNext()) {
                ISSABasicBlock nextBB = succNodes.next();

                String current = bb.getLastInstructionIndex() < 0 || bb.getLastInstruction() == null ? "BB" + blockNo : ("\"" + getLabel(bb.getLastInstruction(), sTable) + "\"");
                String next = nextBB.getLastInstructionIndex() < 0 || nextBB.getLastInstruction() == null ? "BB" + nextBB.getNumber() : ("\"" + getLabel(nextBB.getLastInstruction(), sTable) + "\"");

                String label = "\"" + (nextBB.getNumber() == ifTrueBlockNo ? "true" : (nextBB.getNumber() == ifFalseBlockNo ? "false" : "")) + "\"";

                edgeStringBuffer.append(format("\t%s -> %s[ltail=cluster_%d,lhead=cluster_%d,label=%s];\n",
                        current, next, bb.getNumber(), nextBB.getNumber(), label));

            }
        }

        dotStringBuffer.append("\t// Inter-blocks edges\n").append(edgeStringBuffer);

        dotStringBuffer.append("}");

        try (FileWriter fw = new FileWriter(dotFile)) {
            fw.write(dotStringBuffer.toString());
        }
    }

    private ControlFlowGraph<SSAInstruction, ISSABasicBlock> getControlFlowGraph() {
        IR ir = node.getIR();
        return usePrunedCFG ?
                ExceptionPrunedCFG.make(ir.getControlFlowGraph()) :
                ir.getControlFlowGraph();
    }

    /**
     * Computes a label for a node, escaping any special characters
     *
     * @param instruction instruction to be plotted
     * @param symbolTable symbol table
     * @return a escaped string
     */
    private static String getLabel(SSAInstruction instruction, SymbolTable symbolTable) {
        return StringEscapeUtils.escapeJava(instruction.toString(symbolTable));
    }

}
