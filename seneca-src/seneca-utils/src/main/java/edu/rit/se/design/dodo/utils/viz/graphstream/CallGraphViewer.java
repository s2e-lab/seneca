package edu.rit.se.design.dodo.utils.viz.graphstream;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swing_viewer.util.DefaultMouseManager;
import org.graphstream.ui.view.Viewer;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Displays callgraphs in a UI.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class CallGraphViewer {
    private static final String NODE_STYLE =
            "node {shape:box; fill-color: #e9ffde; text-alignment: center; size-mode: fit;} "
                    + "node.selected { fill-color: red; } "
                    + "node.primordial { fill-color: #ffcfcf; } "
                    + "node.synthetic { fill-color: #9984a3; } "
                    + "node.extension { fill-color: #defffe; } ";
    private static final String EDGE_STYLE = "edge {arrow-shape: arrow; arrow-size: 20px, 4px;} ";

    private static String nodeAsString(CGNode cgNode) {
        IMethod method = cgNode.getMethod();
        return String.format("%s.%s(%s)",
                method.getDeclaringClass().getName().getClassName(),
                method.getName(),
                StringUtils.repeat("_",
                        method.isStatic() ?
                                method.getNumberOfParameters() :
                                method.getNumberOfParameters() - 1));
    }

    //
//
//    /**
//     * Renders an application-only version of the callgraph
//     *
//     * @param title the title
//     * @param cg    callgraph
//     */
//    public static void renderCallgraph(String title, CallGraph cg) {
//        renderCallgraph(title, cg, true);
//    }
//
//    /**
//     * Renders a callgraph in a UI frame using the GraphStream API.
//     *
//     * @param title the title of the graph.
//     * @param cg    a system's callgraph
//     */
//    public static void renderCallgraph(String title, CallGraph cg, boolean applicationOnly) {
//        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
//        Graph uiGraph = new MultiGraph(title);
//        uiGraph.addAttribute("ui.default.title", title);
//
//        Set<String> visited = new HashSet<>();
//        Queue<Integer> toVisit = cg.getEntrypointNodes().stream().map(cgNode -> cgNode.getGraphNodeId()).collect(Collectors.toCollection(LinkedList::new));
//
//        uiGraph.addAttribute("ui.stylesheet", NODE_STYLE + EDGE_STYLE);
//
//
//        while (!toVisit.isEmpty()) {
//            CGNode currentNode = cg.getNode(toVisit.poll());
//            String nodeId = String.valueOf(currentNode.getGraphNodeId());
//
//
//            if (!visited.contains(nodeId)) {
//                Node node = uiGraph.addNode(nodeId);
//                node.addAttribute("ui.label", nodeAsString(currentNode));
//                visited.add(nodeId);
//            }
//            cg.getSuccNodes(currentNode).forEachRemaining(nextNode -> {
//                String succNodeId = String.valueOf(nextNode.getGraphNodeId());
//
//                if (!applicationOnly || isApplicationScope(nextNode)) {
//
//                    if (!visited.contains(succNodeId)) {
//                        toVisit.add(nextNode.getGraphNodeId());
//                        Node succUiNode = uiGraph.addNode(succNodeId);
//                        succUiNode.addAttribute("ui.label", nodeAsString(nextNode));
//                        visited.add(succNodeId);
//                    }
//                    // edge from nodeId to succNodeId
//                    // the edge's identifier is a combination of the from/to
//                    uiGraph.addEdge(nodeId + "_" + succNodeId, nodeId, succNodeId, true);
//                }
//            });
//
//        }
//
//        Viewer viewer = uiGraph.display(false);
////        viewer.getDefaultView().setMouseManager(new GraphMouseManager());
//        viewer.enableAutoLayout(new HierarchicalLayout());
////        viewer.enableAutoLayout(new LinLog());
//
//
//    }
//
//
    public static void renderCollapsibleCallgraph(String title, CallGraph cg) {
//        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        System.setProperty("org.graphstream.ui", "swing");
        Graph uiGraph = new MultiGraph(title);

        Set<String> visited = new HashSet<>();

        uiGraph.setAttribute("ui.default.title", title);
        uiGraph.setAttribute("ui.stylesheet", NODE_STYLE + EDGE_STYLE);


        String[] rootIds = new String[cg.getEntrypointNodes().size()];
        int i = 0;
        for (CGNode entrypointNode : cg.getEntrypointNodes()) {
            String nodeId = String.valueOf(entrypointNode.getGraphNodeId());
            rootIds[i++] = nodeId;
            ClassLoaderReference clr = entrypointNode.getMethod().getDeclaringClass().getClassLoader().getReference();
            if (!visited.contains(nodeId)) {
                Node node = uiGraph.addNode(nodeId);
                node.setAttribute("ui.label", nodeAsString(entrypointNode));
                visited.add(nodeId);
                computeGraphNodeClass(clr, node);
            }
        }

        boolean enableAutolayout = false;
        Viewer viewer = uiGraph.display(enableAutolayout);
        viewer.getDefaultView().enableMouseOptions();
        viewer.getDefaultView().setMouseManager(new DefaultMouseManager() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                GraphicElement element = view.findGraphicElementAt(this.getManagedTypes(), (double) e.getX(), (double) e.getY());//.findNodeOrSpriteAt(e.getX(), e.getY());
                if (element == null) return;
                System.out.println("element=" + element);
                String nodeId = element.getId();
                CGNode node = cg.getNode(Integer.valueOf(nodeId));
                cg.getSuccNodes(node).forEachRemaining(nextNode -> {
                    String nextNodeId = String.valueOf(nextNode.getGraphNodeId());
                    ClassLoaderReference clr = node.getMethod().getDeclaringClass().getClassLoader().getReference();
                    if (!visited.contains(nextNodeId)) {
                        Node succUiNode = uiGraph.addNode(nextNodeId);
                        succUiNode.setAttribute("ui.label", nodeAsString(nextNode));
                        visited.add(nextNodeId);
                        computeGraphNodeClass(clr, succUiNode);
                    }
                    String edgeId = nodeId + "_" + nextNodeId;
                    if (uiGraph.getEdge(edgeId) == null)
                        uiGraph.addEdge(edgeId, nodeId, nextNodeId, true);
                });
            }
        });

        if (!enableAutolayout) {
            RankDirHierarchicalLayout layout = new RankDirHierarchicalLayout(RankDirHierarchicalLayout.Rendering.HORIZONTAL);
            layout.setRoots(rootIds);
            viewer.enableAutoLayout(layout);
        }
//        RankDirHierarchicalLayout hierarchicalLayout = new RankDirHierarchicalLayout(HORIZONTAL);
//        hierarchicalLayout.setRoots(rootIds);
//        viewer.enableAutoLayout(hierarchicalLayout);
    }

    private static void computeGraphNodeClass(ClassLoaderReference clr, Node node) {
        if (clr.equals(ClassLoaderReference.Primordial))
            node.setAttribute("ui.class", "primordial");
        else if (clr.equals(ClassLoaderReference.Extension))
            node.setAttribute("ui.class", "extension");
        else if (clr.equals(ClassLoaderReference.Application))
            node.setAttribute("ui.class", "application");
        else
            node.setAttribute("ui.class", "synthetic");
    }


    public static void main(String[] args) throws IOException {
        Graph graph = new SingleGraph("Tutorial 1");
        graph.setAttribute("ui.stylesheet", NODE_STYLE + EDGE_STYLE);
        graph.setStrict(false);
        graph.setAutoCreate(true);
        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");
        graph.addEdge("AD", "A", "D");
        graph.addEdge("DE", "D", "E");
        graph.addEdge("DF", "D", "F");
        graph.addEdge("EF", "E", "F");
        System.setProperty("org.graphstream.ui", "swing");


        Viewer viewer = graph.display();

        viewer.getDefaultView().enableMouseOptions();
        viewer.getDefaultView().setMouseManager(new DefaultMouseManager());
//        viewer.getDefaultView().setMouseManager(
//                        new MouseOverMouseManager(EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));


    }


}

