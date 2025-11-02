package graph.common;
import java.util.*;

public class Graph {
    private final int vertices;
    private final List<List<Edge>> adjacencyList;
    private final Map<String, Integer> taskNameToId;
    private final Map<Integer, String> idToTaskName;

    public static class Edge {
        public final int to;
        public final double weight;

        public Edge(int to, double weight) {
            this.to = to;
            this.weight = weight;
        }

        public Edge(int to) {
            this(to, 1.0);
        }
    }

    public Graph(int vertices) {
        this.vertices = vertices;
        this.adjacencyList = new ArrayList<>(vertices);
        this.taskNameToId = new HashMap<>();
        this.idToTaskName = new HashMap<>();

        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, double weight) {
        if (u < 0 || u >= vertices || v < 0 || v >= vertices) {
            throw new IllegalArgumentException("Invalid vertex index");
        }
        adjacencyList.get(u).add(new Edge(v, weight));
    }

    public void addEdge(int u, int v) {
        addEdge(u, v, 1.0);
    }

    public void setTaskName(int id, String name) {
        taskNameToId.put(name, id);
        idToTaskName.put(id, name);
    }

    public String getTaskName(int id) {
        return idToTaskName.getOrDefault(id, "Task_" + id);
    }

    public Integer getTaskId(String name) {
        return taskNameToId.get(name);
    }

    public int getVertices() {
        return vertices;
    }

    public List<Edge> getAdjacent(int u) {
        return adjacencyList.get(u);
    }

    public Graph reverse() {
        Graph reversed = new Graph(vertices);

        for (Map.Entry<Integer, String> entry : idToTaskName.entrySet()) {
            reversed.setTaskName(entry.getKey(), entry.getValue());
        }

        for (int u = 0; u < vertices; u++) {
            for (Edge edge : adjacencyList.get(u)) {
                reversed.addEdge(edge.to, u, edge.weight);
            }
        }

        return reversed;
    }

    public int countEdges() {
        int count = 0;
        for (List<Edge> edges : adjacencyList) {
            count += edges.size();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph with ").append(vertices).append(" vertices and ")
                .append(countEdges()).append(" edges:\n");
        for (int u = 0; u < vertices; u++) {
            sb.append(getTaskName(u)).append(" -> ");
            for (Edge edge : adjacencyList.get(u)) {
                sb.append(getTaskName(edge.to))
                        .append("(").append(edge.weight).append(") ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
