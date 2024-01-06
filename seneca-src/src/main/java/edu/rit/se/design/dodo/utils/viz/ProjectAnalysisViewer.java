package edu.rit.se.design.dodo.utils.viz;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import org.apache.commons.text.StringEscapeUtils;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static edu.rit.se.design.dodo.utils.wala.WalaUtils.isApplicationScope;
import static edu.rit.se.design.dodo.utils.wala.WalaUtils.methodToString;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.lang.String.format;

/**
 * This implementation is based of {@link com.ibm.wala.viz.viewer.WalaViewer}. However, this viewer is tailored to visualize the application-only slice of the  callgraph.
 * <p>
 * This implementation mark some variables with coloring if the set of tainted pointers is provided.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class ProjectAnalysisViewer extends JFrame {

    /**
     * @param cg              the callgraph
     * @param taintedPointers if provided, we highlight the variables on this set
     * @param pa              results of the pointer analysis
     * @param prune           if enabled, application-only nodes are rendered in the viewer (please notice that we don't check for loop-backs via primordial scope)
     */
    public ProjectAnalysisViewer(CallGraph cg, Set<PointerKey> taintedPointers, PointerAnalysis pa, boolean prune, File srcFolder) {
        setNativeLookAndFeel();
        if (srcFolder != null && !srcFolder.exists())
            throw new IllegalArgumentException(srcFolder.getAbsolutePath() + " do not exist");

        // set up the window to be as big as the screen
        Dimension screenSize = getDefaultToolkit().getScreenSize();
        this.setSize(screenSize);


        // creates the tabbed pane
        int dividerLocation = (int) screenSize.getWidth() / 3;
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(
                "Call Graph (Pruned=" + prune + ")",
                new PrunedCgPanel(cg, taintedPointers, dividerLocation, prune, srcFolder)
        );
        if (pa != null)
            tabbedPane.add("Pointer Analysis Graph", /*new PaPanel(cg, pa)*/ new PointerAnalysisPanel(cg, pa));


        this.setExtendedState(MAXIMIZED_BOTH);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.setTitle("Wala Viewer");
        this.add(tabbedPane);
        this.setVisible(true);
    }

    /**
     * @param cg              the callgraph
     * @param taintedPointers if provided, we highlight the variables on this set
     * @param pa              results of the pointer analysis
     * @param prune           if enabled, application-only nodes are rendered in the viewer (please notice that we don't check for loop-backs via primordial scope)
     */
    public ProjectAnalysisViewer(CallGraph cg, Set<PointerKey> taintedPointers, PointerAnalysis pa, boolean prune) {
        this(cg, taintedPointers, null, prune, null);
    }

    public ProjectAnalysisViewer(CallGraph cg, Set<PointerKey> taintedPointers, boolean prune) {
        this(cg, taintedPointers, null, prune);
    }

    public ProjectAnalysisViewer(CallGraph cg) {
        this(cg, null, true);
    }

    public static void setNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Panel for visualizing the results of the pointer analysis
 */
class PointerAnalysisPanel extends JPanel {
    private final CallGraph cg;
    private Map<CGNode, Set<PointerKey>> pointerKeysPerNode;
    private PointerAnalysis pa;

    private JTextField jFieldNodeId;
    private JTextArea jTextArea;

    public PointerAnalysisPanel(CallGraph cg, PointerAnalysis pa) {
        super(new BorderLayout());
        this.cg = cg;
        this.pa = pa;
        this.pointerKeysPerNode = new HashMap<>();
        pa.getPointerKeys().forEach(pk -> {
            if (pk instanceof AbstractLocalPointerKey) {
                CGNode node = ((AbstractLocalPointerKey) pk).getNode();
                if (!this.pointerKeysPerNode.containsKey(node)) {
                    this.pointerKeysPerNode.put(node, new HashSet<>());
                }
                this.pointerKeysPerNode.get(node).add((PointerKey) pk);
            }
        });


        this.jFieldNodeId = new JTextField();
        this.jFieldNodeId.addActionListener(e ->
                this.jTextArea.setText(getPaGraph(e.getActionCommand()))
        );
        this.add(this.jFieldNodeId, BorderLayout.PAGE_START);

        this.jTextArea = new JTextArea();
        this.jTextArea.setEditable(false);

        this.add(new JScrollPane(this.jTextArea), BorderLayout.CENTER);
    }

    private String getPaGraph(String actionCommand) {
        CGNode node = cg.getNode(Integer.parseInt(actionCommand));
        Set<PointerKey> pointerKeys = this.pointerKeysPerNode.get(node);

        if (pointerKeys != null) {
            StringBuilder str = new StringBuilder();
            for (PointerKey pointerKey : pointerKeys) {
                str.append(pointerKey).append("\n");
                for (Object ik : pa.getPointsToSet(pointerKey)) {
                    str.append("\t").append(ik).append("\n");
                }
            }
            return str.toString();
        }

        return "null";
    }
}

class SourceViewer extends JPanel {

}

/**
 * Panel for visualizing the callgraph.
 */
class PrunedCgPanel extends JSplitPane {

    private static final long serialVersionUID = -4094408933344852549L;
    private final CallGraph cg;
    private Map<CGNode, Set<PointerKey>> taintedPointersPerNode;
    private boolean applicationOnly;

    public PrunedCgPanel(CallGraph cg, Set<PointerKey> taintedPointers, int dividerLocation, boolean applicationOnly, File srcFolder) {
        this.applicationOnly = applicationOnly;
        this.cg = cg;
        this.taintedPointersPerNode = computeTaintedPointerPerNode(taintedPointers);
        this.setDividerLocation(dividerLocation);

        // configures the tree of nodes and call site references
        JTree tree = buildTree();
        this.setLeftComponent(new JScrollPane(tree));

        final HtmlIrViewer irViewer = new HtmlIrViewer();
        final SourceViewer sourceViewer = new SourceViewer();
        if (srcFolder != null) {
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setDividerLocation(500);
            splitPane.setLeftComponent(irViewer);
            splitPane.setRightComponent(sourceViewer);
            this.setRightComponent(splitPane);
        } else {
            this.setRightComponent(irViewer);
        }

        // add event listener
        tree.addTreeSelectionListener(e -> {
            TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
            if (newLeadSelectionPath == null) return;

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) newLeadSelectionPath.getLastPathComponent();
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof CGNode) {
                CGNode node = (CGNode) userObject;
                irViewer.setIR(node, taintedPointersPerNode.get(node));
            } else if (userObject instanceof CallSiteReference) {
                CGNode parentNode = (CGNode) ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
                irViewer.setIRAndPc(parentNode, ((CallSiteReference) userObject).getProgramCounter(), taintedPointersPerNode.get(parentNode));
            }
        });
    }

    private Map<CGNode, Set<PointerKey>> computeTaintedPointerPerNode(Set<PointerKey> taintedPointers) {
        Map<CGNode, Set<PointerKey>> result = new HashMap<>();
        if (taintedPointers == null) return result;

        for (PointerKey p : taintedPointers) {
            if (p instanceof LocalPointerKey) {
                CGNode cgNode = ((LocalPointerKey) p).getNode();
                if (result.containsKey(cgNode)) result.get(cgNode).add(p);
                else result.put(cgNode, new HashSet<>(Arrays.asList(p)));
            } else if (p instanceof InstanceFieldKey) {
//                ((InstanceFieldKey) p).getInstanceKey().getCreationSites(cg).forEachRemaining(pair -> {
//                    if (result.containsKey(pair.fst)) result.get(pair.fst).add(p);
//                    else result.put(pair.fst, new HashSet<>(Arrays.asList(p)));
//                });
            } else if (p instanceof ReturnValueKey) {
                CGNode cgNode = ((ReturnValueKey) p).getNode();
                if (result.containsKey(cgNode)) result.get(cgNode).add(p);
                else result.put(cgNode, new HashSet<>(Arrays.asList(p)));
            } else {
                assert false;
            }
        }
        return result;
    }

    private JTree buildTree() {
        CGNode cgRoot = cg.getFakeRootNode();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(cgRoot);
        expandNode(root);
        JTree tree = new CustomJTree(root);
        tree.setCellRenderer(new TreeCellRenderer() {
            private JLabel label = new JLabel();

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

                ClassLoaderReference reference = (userObject instanceof CallSiteReference) ?
                        ((CallSiteReference) userObject).getDeclaredTarget().getDeclaringClass().getClassLoader()
                        : ((CGNode) userObject).getMethod().getDeclaringClass().getClassLoader().getReference();

                IClassHierarchy cha = cg.getClassHierarchy();

                IMethod m = (userObject instanceof CallSiteReference) ?
                        cha.resolveMethod(((CallSiteReference) userObject).getDeclaredTarget())
                        : ((CGNode) userObject).getMethod();

                if (m != null && m instanceof SyntheticMethod)
                    label.setIcon(new ImageIcon(getClass().getResource("/synthetic.png")));
                else if (reference.equals(ClassLoaderReference.Application))
                    label.setIcon(new ImageIcon(getClass().getResource("/application.png")));
                else if (reference.equals(ClassLoaderReference.Primordial))
                    label.setIcon(new ImageIcon(getClass().getResource("/primordial.png")));
                else if (reference.equals(ClassLoaderReference.Extension))
                    label.setIcon(new ImageIcon(getClass().getResource("/extension.png")));


                if (selected) label.setBackground(new Color(136, 156, 195));
                else label.setBackground(Color.WHITE);

                label.setOpaque(true);
                label.setText(tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus));
                return label;
            }
        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path != null) {
                    DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    expandNode(lastNode);
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        return tree;
    }

    private void expandNode(DefaultMutableTreeNode treeNode) {
        expandNode(treeNode, 3);
    }

    private void expandNode(DefaultMutableTreeNode treeNode, int rec) {

        if (rec == 0) return;

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof CGNode) {
            if (applicationOnly && (!isApplicationScope((CGNode) userObject) && !cg.getFakeRootNode().equals(userObject)))
                return;
        }


        if (treeNode.getChildCount() == 0) {
            List<DefaultMutableTreeNode> newChildren = new ArrayList<DefaultMutableTreeNode>();

            if (userObject instanceof CGNode) {
                CGNode cgNode = (CGNode) userObject;
                for (Iterator<CallSiteReference> iter = cgNode.iterateCallSites(); iter.hasNext(); ) {
                    CallSiteReference csr = iter.next();
                    // Enable the if condition above to enable callsite references to non-application scope targets
                    /* if (cg.getPossibleTargets(cgNode, csr).stream().anyMatch(n ->  !applicationOnly || isApplicationScope(n))) */
                    newChildren.add(new DefaultMutableTreeNode(csr));
                }
            } else {
                assert userObject instanceof CallSiteReference;
                CallSiteReference csr = (CallSiteReference) userObject;
                CGNode cgNode = (CGNode) ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
                Set<CGNode> successors = cg.getPossibleTargets(cgNode, csr).stream()
                        .filter(n -> !applicationOnly || isApplicationScope(n))
                        .collect(Collectors.toSet());
                for (CGNode successor : successors) {
                    newChildren.add(new DefaultMutableTreeNode(successor));
                }
            }

            newChildren.forEach(treeNode::add);
        }

        for (int i = 0; i < treeNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
            expandNode(child, rec - 1);
        }
    }


    private String getSourceFileName(CGNode node) {
        IMethod method = node.getMethod();
        IClassLoader loader = method.getDeclaringClass().getClassLoader();
        return loader.getSourceFileName(method.getDeclaringClass());
    }
}

/**
 * Creates a view of the IR with coloring.
 */
class HtmlIrViewer extends JPanel {

    private JTextField jTextMethodName;
    private JTextField jTextTaintedPointers;
    private JEditorPane jEditorPane;
    private HTMLEditorKit kit;
    private boolean printBasicBlock = false;

    public HtmlIrViewer() {
        super(new BorderLayout());
        this.jEditorPane = new JEditorPane();
        this.jEditorPane.setEditable(false);
        this.jTextMethodName = new JTextField("IR");
        this.jTextMethodName.setEditable(false);
        this.add(jTextMethodName, BorderLayout.PAGE_START);

        // create a scrollpane; modify its attributes as desired
        JScrollPane scrollPane = new JScrollPane(jEditorPane/*, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED*/);


        // add an html editor kit
        this.kit = new HTMLEditorKit();
        this.jEditorPane.setEditorKit(kit);

        // add some styles to the html
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".tainted {color: #ff0000; background-color:#ffff99;font-weight: bold; }");
        styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
        styleSheet.addRule("p {margin: 2px 0px; }"); // top and bottom margins are X px

        // create a document, set it on the jEditorPane, then add the html
        Document doc = kit.createDefaultDocument();
        this.jEditorPane.setDocument(doc);

        this.add(scrollPane, BorderLayout.CENTER);

        //
        this.jTextTaintedPointers = new JTextField("Tainted Variables");
        this.jTextTaintedPointers.setHighlighter(null);
        this.jTextTaintedPointers.setEditable(false);
        this.add(jTextTaintedPointers, BorderLayout.PAGE_END);

    }

    public void setIRAndPc(CGNode node, int programCounter, Set<PointerKey> taintedPointers) {
        setIR(node, taintedPointers);
    }

    public void setIR(CGNode node, Set<PointerKey> taintedPointers) {
        this.jTextMethodName.setText("ID: " + node.getGraphNodeId() + " Method: " + node.getMethod());
        IR ir = node.getIR();
        if (ir == null) {
            this.jEditorPane.setText("null IR");
        } else {
            try {
                // computes the tainted variables
                List<Integer> taintedVariables = taintedPointers == null ? null :
                        taintedPointers.stream()
                                .filter(p -> p instanceof LocalPointerKey)
                                .map(p -> (LocalPointerKey) p)
                                .map(p -> p.getValueNumber())
                                .collect(Collectors.toList());


                BufferedReader br = new BufferedReader(new StringReader(ir.toString()));
                String line;

                // ignores the printing of basic blocks
                while ((line = br.readLine()) != null)
                    if (line.equals("Instructions:")) break;

                // create some simple html as a string
                StringBuilder htmlString = new StringBuilder();

                // prints instructions only
                while ((line = br.readLine()) != null) {
                    if (printBasicBlock || !line.startsWith("BB")) {
                        htmlString.append("<p>")
                                .append(processIrLine(line, taintedVariables))
                                .append("</p>");
                    }
                }
                // updates text
                this.jEditorPane.setText(htmlString.toString());
                // updates the tainted pointers textarea
                if (taintedPointers != null)
                    this.jTextTaintedPointers.setText("Tainted Variables: " + taintedVariables.toString());
                else
                    this.jTextTaintedPointers.setText("Tainted Variables: ");
            } catch (IOException e) {
                // ???
                assert false;
            }
        }
    }

    private String processIrLine(String line, List<Integer> taintedVariables) {

        String processedLine = StringEscapeUtils.escapeHtml4(line).replace(" ", "&nbsp;");

        if (taintedVariables != null) {
            for (Integer taintedVariable : taintedVariables) {
                processedLine = processedLine.replace(format("v%d&nbsp;", taintedVariable), format("<span class=\"tainted\">v%d</span>&nbsp;", taintedVariable));
                processedLine = processedLine.replace(format("v%d,", taintedVariable), format("<span class=\"tainted\">v%d</span>,", taintedVariable));
                processedLine = processedLine.replace(format("v%d(", taintedVariable), format("<span class=\"tainted\">v%d</span>(", taintedVariable));
            }
        }
        return processedLine;
    }


}


class CustomJTree extends JTree {
    public CustomJTree(TreeNode root) {
        super(root);
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof CallSiteReference) {
                CallSiteReference callSiteReference = (CallSiteReference) userObject;
                String csrString = callSiteReference.toString();
                MethodReference declaredTarget = callSiteReference.getDeclaredTarget();
                return csrString.replace(declaredTarget.toString(), methodToString(declaredTarget));
            }
            if (userObject instanceof CGNode) {
                CGNode cgNode = (CGNode) userObject;
                String cgNodeString = cgNode.toString();
                MethodReference methodReference = cgNode.getMethod().getReference();
                return cgNodeString.replace(methodReference.toString(), methodToString(methodReference));
            }
        }
        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}