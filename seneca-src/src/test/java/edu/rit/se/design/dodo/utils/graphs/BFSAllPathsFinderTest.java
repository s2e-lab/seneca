package edu.rit.se.design.dodo.utils.graphs;

import com.ibm.wala.util.graph.NumberedGraph;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static edu.rit.se.design.dodo.utils.graphs.PathFinderTestUtils.createGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Joanna C. S. Santos jds5109@rit.edu
 */
public class BFSAllPathsFinderTest {



    @Test
    public void findExampleGeeksForGeeks() {
        // example from here: https://www.geeksforgeeks.org/print-paths-given-source-destination-using-bfs/
        NumberedGraph<String> graph = createGraph("010302202113");
        BFSAllPathsFinder<String> finder = new BFSAllPathsFinder<>(graph, s -> s.equals("3"), "2");

        List<List<String>> foundPaths = finder.find();
        assertEquals(3, foundPaths.size());
        assertTrue(foundPaths.contains(Arrays.asList("2", "0", "3")));
        assertTrue(foundPaths.contains(Arrays.asList("2", "1", "3")));
        assertTrue(foundPaths.contains(Arrays.asList("2", "0", "1", "3")));
    }


    // example from here: https://github.com/wala/WALA/blob/master/com.ibm.wala.core.tests/src/com/ibm/wala/core/tests/basic/PathFinderTest.java
    @Test
    public void findExampleWala1() {
        NumberedGraph<String> graph = createGraph("ABBCBDCECFDGDHEIFIGJHJJKIKKL");
        BFSAllPathsFinder<String> finder = new BFSAllPathsFinder<>(graph, s -> s.equals("L"), "A");
        List<List<String>> foundPaths = finder.find();
        assertEquals(4, foundPaths.size());
    }

    @Test
    public void findExampleWala2() {
        NumberedGraph<String> graph = createGraph("ABBCBDCECFDGDHEIFIGJHJJKIKKCKL");
        BFSAllPathsFinder<String> finder = new BFSAllPathsFinder<>(graph, s -> s.equals("L"), "A");
        List<List<String>> foundPaths = finder.find();
        assertEquals(4, foundPaths.size());
    }


    @Test
    public void findExampleWala3() {
        NumberedGraph<String> graph = createGraph("ABBHBCBDCECFDGDHEIFIGJHJJKIKKCKL");
        BFSAllPathsFinder<String> finder = new BFSAllPathsFinder<>(graph, s -> s.equals("L"), "A");
        List<List<String>> foundPaths = finder.find();
        assertEquals(5, foundPaths.size());
    }


    @Test
    public void findExampleWala4() {
        NumberedGraph<String> graph = createGraph("ABACADAEBABCBDBECACBCDCEDADBDCDEEAEBECED");
        BFSAllPathsFinder<String> finder = new BFSAllPathsFinder<>(graph, s -> s.equals("E"), "A");
        List<List<String>> foundPaths = finder.find();
        assertEquals(1 + 3 + 6 + 6, foundPaths.size());
    }
}
