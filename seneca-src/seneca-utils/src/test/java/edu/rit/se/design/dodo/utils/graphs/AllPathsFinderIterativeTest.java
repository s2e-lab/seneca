package edu.rit.se.design.dodo.utils.graphs;

import com.ibm.wala.util.graph.NumberedGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.rit.se.design.dodo.utils.graphs.PathFinderTestUtils.createGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Joanna C. S. Santos jds5109@rit.edu
 */
public class AllPathsFinderIterativeTest {
    @Test
    public void findExampleGeeksForGeeks() {
        // example from here: https://www.geeksforgeeks.org/print-paths-given-source-destination-using-bfs/
        NumberedGraph<String> graph = createGraph("010302202113");
        AllPathsFinderIterative<String> finder = new AllPathsFinderIterative<>(graph, s -> s.equals("3"), "2");

        List<List<String>> foundPaths = new ArrayList<>();
        List<String> path;
        while ((path = finder.find()) != null) {
            foundPaths.add(path);
        }
        assertEquals(3, foundPaths.size());
        assertTrue(foundPaths.contains(Arrays.asList("2", "0", "3")));
        assertTrue(foundPaths.contains(Arrays.asList("2", "1", "3")));
        assertTrue(foundPaths.contains(Arrays.asList("2", "0", "1", "3")));
    }


    // example from here: https://github.com/wala/WALA/blob/master/com.ibm.wala.core.tests/src/com/ibm/wala/core/tests/basic/PathFinderTest.java
    @Test
    public void findExampleWala1() {
        NumberedGraph<String> graph = createGraph("ABBCBDCECFDGDHEIFIGJHJJKIKKL");
        AllPathsFinderIterative<String> finder = new AllPathsFinderIterative<>(graph, s -> s.equals("L"), "A");
        List<List<String>> foundPaths = new ArrayList<>();
        List<String> path;
        while ((path = finder.find()) != null) {
            foundPaths.add(path);
        }
        assertEquals(4, foundPaths.size());
    }

    @Test
    public void findExampleWala2() {
        NumberedGraph<String> graph = createGraph("ABBCBDCECFDGDHEIFIGJHJJKIKKCKL");
        AllPathsFinderIterative<String> finder = new AllPathsFinderIterative<>(graph, s -> s.equals("L"), "A");
        List<List<String>> foundPaths = new ArrayList<>();
        List<String> path;
        while ((path = finder.find()) != null) {
            foundPaths.add(path);
        }
        assertEquals(4, foundPaths.size());
    }


    @Test
    public void findExampleWala3() {
        NumberedGraph<String> graph = createGraph("ABBHBCBDCECFDGDHEIFIGJHJJKIKKCKL");
        AllPathsFinderIterative<String> finder = new AllPathsFinderIterative<>(graph, s -> s.equals("L"), "A");
        List<List<String>> foundPaths = new ArrayList<>();
        List<String> path;
        while ((path = finder.find()) != null) {
            foundPaths.add(path);
        }
        assertEquals(5, foundPaths.size());
    }


    @Test
    public void findExampleWala4() {
        NumberedGraph<String> graph = createGraph("ABACADAEBABCBDBECACBCDCEDADBDCDEEAEBECED");
        AllPathsFinderIterative<String> finder = new AllPathsFinderIterative<>(graph, s -> s.equals("E"), "A");
        List<List<String>> foundPaths = new ArrayList<>();
        List<String> path;
        while ((path = finder.find()) != null) {
            foundPaths.add(path);
        }
        assertEquals(1 + 3 + 6 + 6, foundPaths.size());
    }

}
