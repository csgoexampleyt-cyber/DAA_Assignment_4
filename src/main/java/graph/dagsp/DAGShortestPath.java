package graph.dagsp;

import graph.common.BasicMetrics;
import graph.common.Graph;
import graph.common.Metrics;
import graph.topo.TopologicalSort;
import java.util.*;

public class DAGShortestPath {
    private final Graph graph;
    private final Metrics metrics;

    public DAGShortestPath(Graph graph, Metrics metrics) {
        this.graph = graph;
        this.metrics = metrics;
    }

    public PathResult shortestPaths(int source) {
        int n = graph.getVertices();
        double[] dist = new double[n];
        int[] pred = new int[n];

        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(pred, -1);
        dist[source] = 0;

        metrics.startTiming();

        TopologicalSort topo = new TopologicalSort(graph, new BasicMetrics());
        List<Integer> order = topo.sort();

        if (order.size() != n) {
            metrics.stopTiming();
            return new PathResult(dist, pred, metrics, false);
        }

        for (int u : order) {
            if (dist[u] != Double.POSITIVE_INFINITY) {
                for (Graph.Edge edge : graph.getAdjacent(u)) {
                    int v = edge.to;
                    metrics.incrementCounter("relaxations");

                    if (dist[u] + edge.weight < dist[v]) {
                        dist[v] = dist[u] + edge.weight;
                        pred[v] = u;
                    }
                }
            }
        }

        metrics.stopTiming();

        return new PathResult(dist, pred, metrics, true);
    }

    public PathResult longestPaths(int source) {
        int n = graph.getVertices();
        double[] dist = new double[n];
        int[] pred = new int[n];

        Arrays.fill(dist, Double.NEGATIVE_INFINITY);
        Arrays.fill(pred, -1);
        dist[source] = 0;

        metrics.startTiming();

        TopologicalSort topo = new TopologicalSort(graph, new BasicMetrics());
        List<Integer> order = topo.sort();

        if (order.size() != n) {
            metrics.stopTiming();
            return new PathResult(dist, pred, metrics, false);
        }

        for (int u : order) {
            if (dist[u] != Double.NEGATIVE_INFINITY) {
                for (Graph.Edge edge : graph.getAdjacent(u)) {
                    int v = edge.to;
                    metrics.incrementCounter("relaxations");

                    if (dist[u] + edge.weight > dist[v]) {
                        dist[v] = dist[u] + edge.weight;
                        pred[v] = u;
                    }
                }
            }
        }

        metrics.stopTiming();

        return new PathResult(dist, pred, metrics, true);
    }

    public CriticalPathResult findCriticalPath() {
        int n = graph.getVertices();

        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getAdjacent(u)) {
                inDegree[edge.to]++;
            }
        }

        int source = -1;
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                source = i;
                break;
            }
        }

        if (source == -1) {
            source = 0;
        }

        PathResult result = longestPaths(source);

        int maxVertex = source;
        double maxDist = 0;
        for (int i = 0; i < n; i++) {
            if (result.dist[i] != Double.NEGATIVE_INFINITY && result.dist[i] > maxDist) {
                maxDist = result.dist[i];
                maxVertex = i;
            }
        }

        List<Integer> path = reconstructPath(result.pred, source, maxVertex);

        return new CriticalPathResult(path, maxDist, result.metrics);
    }

    private List<Integer> reconstructPath(int[] pred, int source, int target) {
        List<Integer> path = new ArrayList<>();

        if (pred[target] == -1 && target != source) {
            return path;
        }

        int current = target;
        while (current != -1) {
            path.add(current);
            if (current == source) break;
            current = pred[current];
        }

        Collections.reverse(path);
        return path;
    }

    public static class PathResult {
        public final double[] dist;
        public final int[] pred;
        public final Metrics metrics;
        public final boolean isValid;

        public PathResult(double[] dist, int[] pred, Metrics metrics, boolean isValid) {
            this.dist = dist;
            this.pred = pred;
            this.metrics = metrics;
            this.isValid = isValid;
        }

        public List<Integer> getPath(int source, int target) {
            List<Integer> path = new ArrayList<>();

            if (pred[target] == -1 && target != source) {
                return path;
            }

            int current = target;
            while (current != -1) {
                path.add(current);
                if (current == source) break;
                current = pred[current];
            }

            Collections.reverse(path);
            return path;
        }

        public void printResults(Graph graph, int source) {
            System.out.println("Shortest Paths from " + graph.getTaskName(source));

            if (!isValid) {
                System.out.println("ERROR: Graph contains a cycle!");
                return;
            }

            for (int i = 0; i < dist.length; i++) {
                if (dist[i] != Double.POSITIVE_INFINITY) {
                    System.out.println(graph.getTaskName(i) + ": " + dist[i]);
                    List<Integer> path = getPath(source, i);
                    System.out.print("  Path: ");
                    for (int j = 0; j < path.size(); j++) {
                        System.out.print(graph.getTaskName(path.get(j)));
                        if (j < path.size() - 1) System.out.print(" -> ");
                    }
                    System.out.println();
                }
            }

            System.out.println("\nMetrics");
            System.out.println(metrics.getSummary());
        }
    }

    public static class CriticalPathResult {
        public final List<Integer> path;
        public final double length;
        public final Metrics metrics;

        public CriticalPathResult(List<Integer> path, double length, Metrics metrics) {
            this.path = path;
            this.length = length;
            this.metrics = metrics;
        }

        public void printResults(Graph graph) {
            System.out.println("Critical Path (Longest Path)");
            System.out.println("Length: " + length);
            System.out.print("Path: ");
            for (int i = 0; i < path.size(); i++) {
                System.out.print(graph.getTaskName(path.get(i)));
                if (i < path.size() - 1) System.out.print(" -> ");
            }
            System.out.println("\n");

            System.out.println("Metrics");
            System.out.println(metrics.getSummary());
        }
    }
}