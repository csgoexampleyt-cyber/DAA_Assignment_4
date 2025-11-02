package graph.test;

import graph.common.BasicMetrics;
import graph.common.Graph;
import graph.dagsp.DAGShortestPath;
import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

public class GraphAlgorithmsTest {

    @Test
    public void testTopologicalSort() {
        Graph g = new Graph(4);
        g.setTaskName(0, "A");
        g.setTaskName(1, "B");
        g.setTaskName(2, "C");
        g.setTaskName(3, "D");

        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 3);

        TopologicalSort topo = new TopologicalSort(g, new BasicMetrics());
        List<Integer> order = topo.sort();

        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
    }

    @Test
    public void testCriticalPath() {
        Graph g = new Graph(4);
        g.setTaskName(0, "A");
        g.setTaskName(1, "B");
        g.setTaskName(2, "C");
        g.setTaskName(3, "D");

        g.addEdge(0, 1, 3);
        g.addEdge(1, 2, 5);
        g.addEdge(0, 3, 2);

        DAGShortestPath cp = new DAGShortestPath(g, new BasicMetrics());
        DAGShortestPath.CriticalPathResult result = cp.findCriticalPath();

        assertEquals(8.0, result.length, 0.001);
    }
    @Test
    public void testSCC() {
        Graph g = new Graph(3);
        g.setTaskName(0, "A");
        g.setTaskName(1, "B");
        g.setTaskName(2, "C");

        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);

        TarjanSCC scc = new TarjanSCC(g, new BasicMetrics());
        List<List<Integer>> components = scc.findSCCs();

        assertEquals(1, components.size());
        assertEquals(3, components.get(0).size());
    }
}

