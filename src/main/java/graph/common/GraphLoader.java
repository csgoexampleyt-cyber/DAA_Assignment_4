package graph.common;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GraphLoader {

    public static Graph loadFromJSON(String filename) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        JSONObject json = new JSONObject(content);

        int vertices = json.getInt("vertices");
        Graph graph = new Graph(vertices);

        JSONArray tasks = json.getJSONArray("tasks");
        for (int i = 0; i < tasks.length(); i++) {
            graph.setTaskName(i, tasks.getString(i));
        }

        JSONArray edges = json.getJSONArray("edges");
        for (int i = 0; i < edges.length(); i++) {
            JSONObject edge = edges.getJSONObject(i);
            String from = edge.getString("from");
            String to = edge.getString("to");
            double weight = edge.optDouble("weight", 1.0);

            Integer fromId = graph.getTaskId(from);
            Integer toId = graph.getTaskId(to);

            if (fromId != null && toId != null) {
                graph.addEdge(fromId, toId, weight);
            }
        }

        return graph;
    }

    public static void saveToJSON(Graph graph, String filename) throws Exception {
        JSONObject json = new JSONObject();
        json.put("vertices", graph.getVertices());

        JSONArray tasks = new JSONArray();
        for (int i = 0; i < graph.getVertices(); i++) {
            tasks.put(graph.getTaskName(i));
        }
        json.put("tasks", tasks);

        JSONArray edges = new JSONArray();
        for (int u = 0; u < graph.getVertices(); u++) {
            for (Graph.Edge edge : graph.getAdjacent(u)) {
                JSONObject edgeObj = new JSONObject();
                edgeObj.put("from", graph.getTaskName(u));
                edgeObj.put("to", graph.getTaskName(edge.to));
                edgeObj.put("weight", edge.weight);
                edges.put(edgeObj);
            }
        }
        json.put("edges", edges);

        Files.write(Paths.get(filename), json.toString(2).getBytes());
    }
}
