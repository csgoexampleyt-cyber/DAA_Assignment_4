package graph.scc;

import graph.common.Graph;
import graph.common.Metrics;

import java.util.*;

public class TarjanSCC {
    private final Graph graph;
    private final Metrics metrics;

    private int[] ids;
    private int[] low;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int id;
    private List<List<Integer>> sccs;

    public TarjanSCC(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public List<List<Integer>> findSCCs() {
        int n = graph.getVertices();
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();
        id = 0;

        Arrays.fill(ids, -1);

        metrics.startTiming();

        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfs(i);
            }
        }

        metrics.stopTiming();

        return sccs;
    }

    private void dfs(int u) {
        metrics.incrementCounter("dfs_visits");

        ids[u] = low[u] = id++;
        stack.push(u);
        onStack[u] = true;

        for (Graph.Edge edge : graph.getAdjacent(u)) {
            int v = edge.to;
            metrics.incrementCounter("edge_traversals");

            if (ids[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {

                low[u] = Math.min(low[u], ids[v]);
            }
        }

        if (ids[u] == low[u]) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                scc.add(v);
                metrics.incrementCounter("stack_pops");
            } while (v != u);

            sccs.add(scc);
        }
    }

    public Graph buildCondensationGraph(List<List<Integer>> sccs) {
        int numSCCs = sccs.size();
        Graph condensation = new Graph(numSCCs);

        int[] vertexToSCC = new int[graph.getVertices()];
        for (int i = 0; i < sccs.size(); i++) {
            for (int v : sccs.get(i)) {
                vertexToSCC[v] = i;
            }
            if (sccs.get(i).size() == 1) {
                condensation.setTaskName(i, graph.getTaskName(sccs.get(i).get(0)));
            } else {
                condensation.setTaskName(i, "SCC_" + i + "_" + sccs.get(i).size() + "_tasks");
            }
        }

        Set<String> addedEdges = new HashSet<>();
        for (int u = 0; u < graph.getVertices(); u++) {
            int sccU = vertexToSCC[u];
            for (Graph.Edge edge : graph.getAdjacent(u)) {
                int v = edge.to;
                int sccV = vertexToSCC[v];

                if (sccU != sccV) {
                    String edgeKey = sccU + "->" + sccV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(sccU, sccV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    public SCCResult getResults() {
        List<List<Integer>> sccs = findSCCs();
        Graph condensation = buildCondensationGraph(sccs);
        return new SCCResult(sccs, condensation, metrics);
    }

    public static class SCCResult {
        public final List<List<Integer>> sccs;
        public final Graph condensation;
        public final Metrics metrics;

        public SCCResult(List<List<Integer>> sccs, Graph condensation, Metrics metrics) {
            this.sccs = sccs;
            this.condensation = condensation;
            this.metrics = metrics;
        }

        public void printResults(Graph originalGraph) {
            System.out.println("Strongly Connected Components");
            System.out.println("Total SCCs found: " + sccs.size());
            System.out.println();

            for (int i = 0; i < sccs.size(); i++) {
                List<Integer> scc = sccs.get(i);
                System.out.println("SCC " + i + " (size " + scc.size() + "):");
                for (int v : scc) {
                    System.out.print("  " + originalGraph.getTaskName(v));
                }
                System.out.println();
            }

            System.out.println("\nCondensation Graph");
            System.out.println(condensation);

            System.out.println("Metrics");
            System.out.println(metrics.getSummary());
        }
    }
}