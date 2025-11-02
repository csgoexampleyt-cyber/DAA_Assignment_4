package graph.topo;

import graph.common.Graph;
import graph.common.Metrics;
import java.util.*;

public class TopologicalSort {
    private final Graph graph;
    private final Metrics metrics;

    public TopologicalSort(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public List<Integer> sort() {
        int n = graph.getVertices();
        int[] inDegree = new int[n];

        metrics.startTiming();

        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getAdjacent(u)) {
                inDegree[edge.to]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementCounter("queue_pushes");
            }
        }

        List<Integer> topoOrder = new ArrayList<>();

        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.incrementCounter("queue_pops");
            topoOrder.add(u);

            for (Graph.Edge edge : graph.getAdjacent(u)) {
                int v = edge.to;
                inDegree[v]--;

                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementCounter("queue_pushes");
                }
            }
        }

        metrics.stopTiming();

        if (topoOrder.size() != n) {
            return new ArrayList<>();
        }

        return topoOrder;
    }

    public List<Integer> sortDFS() {
        int n = graph.getVertices();
        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        metrics.startTiming();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfsTopo(i, visited, stack);
            }
        }

        metrics.stopTiming();

        List<Integer> topoOrder = new ArrayList<>();
        while (!stack.isEmpty()) {
            topoOrder.add(stack.pop());
            metrics.incrementCounter("stack_pops");
        }

        return topoOrder;
    }

    private void dfsTopo(int u, boolean[] visited, Stack<Integer> stack) {
        visited[u] = true;
        metrics.incrementCounter("dfs_visits");

        for (Graph.Edge edge : graph.getAdjacent(u)) {
            if (!visited[edge.to]) {
                dfsTopo(edge.to, visited, stack);
            }
        }

        stack.push(u);
        metrics.incrementCounter("stack_pushes");
    }

    public TopoResult getResults() {
        List<Integer> order = sort();
        return new TopoResult(order, metrics, order.size() == graph.getVertices());
    }

    public static class TopoResult {
        public final List<Integer> order;
        public final Metrics metrics;
        public final boolean isValid;

        public TopoResult(List<Integer> order, Metrics metrics, boolean isValid) {
            this.order = order;
            this.metrics = metrics;
            this.isValid = isValid;
        }

        public void printResults(Graph graph) {
            System.out.println("Topological Sort");

            if (!isValid) {
                System.out.println("ERROR: Graph contains a cycle!");
                return;
            }

            System.out.println("Topological Order:");
            for (int i = 0; i < order.size(); i++) {
                System.out.println((i + 1) + ". " + graph.getTaskName(order.get(i)));
            }

            System.out.println("\nMetrics");
            System.out.println(metrics.getSummary());
        }
    }
}