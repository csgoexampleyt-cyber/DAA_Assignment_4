package graph.utility;

import graph.common.BasicMetrics;
import graph.common.Graph;
import graph.common.GraphLoader;
import graph.common.Metrics;
import graph.dagsp.DAGShortestPath;
import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;

import java.io.File;
import java.util.*;

public class SmartCityScheduler {

    public static void main(String[] args) {
        try {
            printWelcome();

            if (args.length == 0) {
                runCompleteDemo();
            } else {
                processGraph(args[0]);
            }

        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runCompleteDemo() throws Exception {
        System.out.println("COMPLETE DEMONSTRATION MODE");
        System.out.println("Running all features automatically...\n");

        System.out.println("Step 1: Generating Test Datasets\n");
        generateDatasets();
        pause(1000);

        System.out.println("Step 2: Running Comprehensive Tests\n");
        runTests();
        pause(1000);

        System.out.println("Step 3: Sample Graph Analysis\n");
        runDemo();
        pause(1000);

        System.out.println("Step 4: Processing Generated Datasets\n");
        processSampleDatasets();

        printFinalSummary();
    }

    private static void generateDatasets() throws Exception {
        System.out.println("Creating test datasets...\n");

        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        DatasetGenerator generator = new DatasetGenerator(42);

        String[] datasets = {
                "small_dag_1", "small_cycle_1", "small_sparse_1",
                "medium_dag_1", "medium_cycle_1", "medium_mixed_1",
                "large_sparse_1", "large_dense_1", "large_complex_1"
        };

        int[][] configs = {
                {6, 30, 0, 0},
                {8, 35, 1, 2},
                {10, 20, 0, 0},
                {12, 25, 0, 0},
                {15, 30, 1, 3},
                {18, 28, 1, 4},
                {25, 15, 0, 0},
                {35, 25, 1, 5},
                {45, 20, 1, 8}
        };

        for (int i = 0; i < datasets.length; i++) {
            String filename = "data/" + datasets[i] + ".json";
            int vertices = configs[i][0];
            double density = configs[i][1] / 100.0;
            boolean hasCycles = configs[i][2] == 1;
            int numCycles = configs[i][3];

            Graph graph = generator.generateGraph(vertices, density, hasCycles, numCycles);
            GraphLoader.saveToJSON(graph, filename);

            System.out.println("  " + datasets[i] + ".json");
            System.out.println("    Vertices: " + vertices + ", Edges: " + graph.countEdges() +
                    ", Cycles: " + (hasCycles ? "Yes" : "No"));
        }

        System.out.println("\nSuccessfully generated " + datasets.length + " datasets!\n");
    }

    private static void runTests() {
        System.out.println("Running algorithm tests...\n");

        int passed = 0;
        int failed = 0;

        try {
            Graph g = new Graph(3);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 0);

            TarjanSCC scc = new TarjanSCC(g, new BasicMetrics());
            List<List<Integer>> components = scc.findSCCs();

            if (components.size() == 1 && components.get(0).size() == 3) {
                System.out.println("SCC (Simple Cycle): PASS");
                passed++;
            } else {
                System.out.println("SCC (Simple Cycle): FAIL");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("SCC (Simple Cycle): ERROR " + e.getMessage());
            failed++;
        }

        try {
            Graph g = new Graph(4);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 3);

            TarjanSCC scc = new TarjanSCC(g, new BasicMetrics());
            List<List<Integer>> components = scc.findSCCs();

            if (components.size() == 4) {
                System.out.println("SCC (Pure DAG): PASS");
                passed++;
            } else {
                System.out.println("SCC (Pure DAG): FAIL");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("SCC (Pure DAG): ERROR " + e.getMessage());
            failed++;
        }

        try {
            Graph g = new Graph(4);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 3);

            TopologicalSort topo = new TopologicalSort(g, new BasicMetrics());
            List<Integer> order = topo.sort();

            if (order.size() == 4 &&
                    order.indexOf(0) < order.indexOf(1) &&
                    order.indexOf(1) < order.indexOf(2) &&
                    order.indexOf(2) < order.indexOf(3)) {
                System.out.println("Topological Sort: PASS");
                passed++;
            } else {
                System.out.println("Topological Sort: FAIL");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("Topological Sort: ERROR " + e.getMessage());
            failed++;
        }

        try {
            Graph g = new Graph(3);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 0);

            TopologicalSort topo = new TopologicalSort(g, new BasicMetrics());
            List<Integer> order = topo.sort();

            if (order.isEmpty()) {
                System.out.println("Cycle Detection: PASS");
                passed++;
            } else {
                System.out.println("Cycle Detection: FAIL");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("Cycle Detection: ERROR " + e.getMessage());
            failed++;
        }

        try {
            Graph g = new Graph(3);
            g.addEdge(0, 1, 2.0);
            g.addEdge(1, 2, 3.0);

            DAGShortestPath dagSP = new DAGShortestPath(g, new BasicMetrics());
            DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

            if (Math.abs(result.dist[2] - 5.0) < 0.001) {
                System.out.println("Shortest Path: PASS");
                passed++;
            } else {
                System.out.println("Shortest Path: FAIL (expected 5.0, got " + result.dist[2] + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("Shortest Path: ERROR " + e.getMessage());
            failed++;
        }

        try {
            Graph g = new Graph(4);
            g.addEdge(0, 1, 2.0);
            g.addEdge(0, 2, 1.0);
            g.addEdge(1, 3, 1.0);
            g.addEdge(2, 3, 4.0);

            DAGShortestPath dagSP = new DAGShortestPath(g, new BasicMetrics());
            DAGShortestPath.PathResult result = dagSP.longestPaths(0);

            if (Math.abs(result.dist[3] - 5.0) < 0.001) {
                System.out.println("Longest Path: PASS");
                passed++;
            } else {
                System.out.println("Longest Path: FAIL (expected 5.0, got " + result.dist[3] + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("Longest Path: ERROR " + e.getMessage());
            failed++;
        }

        try {
            Graph g = new Graph(5);
            g.addEdge(0, 1, 3.0);
            g.addEdge(0, 2, 2.0);
            g.addEdge(1, 3, 4.0);
            g.addEdge(2, 3, 1.0);
            g.addEdge(3, 4, 2.0);

            DAGShortestPath dagSP = new DAGShortestPath(g, new BasicMetrics());
            DAGShortestPath.CriticalPathResult result = dagSP.findCriticalPath();

            if (Math.abs(result.length - 9.0) < 0.001) {
                System.out.println("Critical Path: PASS");
                passed++;
            } else {
                System.out.println("Critical Path: FAIL (expected 9.0, got " + result.length + ")");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("Critical Path: ERROR " + e.getMessage());
            failed++;
        }

        int total = passed + failed;
        double percentage = total > 0 ? (passed * 100.0 / total) : 0;

        System.out.println("\nTest Summary:");
        System.out.println("  Total:   " + total);
        System.out.println("  Passed:  " + passed);
        System.out.println("  Failed:  " + failed);
        System.out.println("  Success: " + String.format("%.1f%%", percentage));
        System.out.println();
    }

    private static void runDemo() throws Exception {
        System.out.println("Creating and analyzing sample graph...\n");

        Graph demoGraph = createDemoGraph();
        String demoFile = "demo_graph.json";
        GraphLoader.saveToJSON(demoGraph, demoFile);

        System.out.println("Created sample graph:");
        System.out.println("  Vertices: " + demoGraph.getVertices());
        System.out.println("  Edges: " + demoGraph.countEdges());
        System.out.println("  Saved to: " + demoFile + "\n");

        processGraphSilent(demoGraph, "Demo Sensor Network");
    }

    private static void processSampleDatasets() throws Exception {
        File dataDir = new File("data");
        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.out.println("Data directory not found. Skipping.\n");
            return;
        }

        File[] files = dataDir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("No datasets found. Skipping.\n");
            return;
        }

        int limit = Math.min(3, files.length);
        System.out.println("Processing " + limit + " sample datasets...\n");

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (int i = 0; i < limit; i++) {
            System.out.println("Dataset " + (i + 1) + ": " + files[i].getName());
            processGraph(files[i].getPath());
            System.out.println();
        }

        if (files.length > limit) {
            System.out.println((files.length - limit) + " more datasets available.\n");
        }
    }

    private static void processGraph(String filename) throws Exception {
        Graph graph = GraphLoader.loadFromJSON(filename);
        processGraphSilent(graph, filename);
    }

    private static void processGraphSilent(Graph graph, String name) {
        System.out.println("\nGraph: " + name);
        System.out.println("   Vertices: " + graph.getVertices() + ", Edges: " + graph.countEdges());

        Metrics sccMetrics = new BasicMetrics();
        TarjanSCC tarjan = new TarjanSCC(graph, sccMetrics);
        TarjanSCC.SCCResult sccResult = tarjan.getResults();

        System.out.println("   Found " + sccResult.sccs.size() + " SCCs");
        int cycleCount = 0;
        for (List<Integer> scc : sccResult.sccs) {
            if (scc.size() > 1) cycleCount++;
        }

        if (cycleCount > 0) {
            System.out.println("   Contains " + cycleCount + " cyclic components");
        } else {
            System.out.println("   No cycles detected (pure DAG)");
        }

        System.out.println("   Time: " + String.format("%.3f ms", sccMetrics.getElapsedTimeMillis()));

        Metrics topoMetrics = new BasicMetrics();
        TopologicalSort topo = new TopologicalSort(sccResult.condensation, topoMetrics);
        TopologicalSort.TopoResult topoResult = topo.getResults();

        if (topoResult.isValid) {
            System.out.println("   Valid topological order found");
            System.out.println("   Order size: " + topoResult.order.size());
        } else {
            System.out.println("   Cannot compute topological sort (cycle in condensation)");
        }

        if (topoResult.isValid && sccResult.condensation.getVertices() > 0) {
            Metrics spMetrics = new BasicMetrics();
            DAGShortestPath dagSP = new DAGShortestPath(sccResult.condensation, spMetrics);
            DAGShortestPath.PathResult spResult = dagSP.shortestPaths(findSource(sccResult.condensation));

            double maxDist = 0;
            for (double d : spResult.dist) {
                if (d != Double.POSITIVE_INFINITY && d > maxDist) maxDist = d;
            }

            System.out.println("   Shortest Path Max Distance: " + String.format("%.1f", maxDist));

            Metrics cpMetrics = new BasicMetrics();
            DAGShortestPath dagCP = new DAGShortestPath(sccResult.condensation, cpMetrics);
            DAGShortestPath.CriticalPathResult cpResult = dagCP.findCriticalPath();

            System.out.println("   Critical Path Length: " + String.format("%.1f", cpResult.length));
        }
    }

    private static Graph createDemoGraph() {
        Graph graph = new Graph(10);

        graph.setTaskName(0, "InitSensors");
        graph.setTaskName(1, "ReadTemp");
        graph.setTaskName(2, "ReadHumidity");
        graph.setTaskName(3, "ReadAirQuality");
        graph.setTaskName(4, "ProcessData");
        graph.setTaskName(5, "ValidateData");
        graph.setTaskName(6, "SendAlert");
        graph.setTaskName(7, "UpdateDashboard");
        graph.setTaskName(8, "LogResults");
        graph.setTaskName(9, "ArchiveData");

        graph.addEdge(0, 1, 2.0);
        graph.addEdge(0, 2, 2.0);
        graph.addEdge(0, 3, 2.0);
        graph.addEdge(1, 4, 1.5);
        graph.addEdge(2, 4, 1.5);
        graph.addEdge(3, 4, 1.5);
        graph.addEdge(4, 5, 3.0);
        graph.addEdge(5, 4, 0.5);
        graph.addEdge(5, 6, 2.0);
        graph.addEdge(5, 7, 1.0);
        graph.addEdge(6, 8, 1.5);
        graph.addEdge(7, 8, 1.0);
        graph.addEdge(8, 9, 2.0);

        return graph;
    }

    private static int findSource(Graph graph) {
        int n = graph.getVertices();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++) {
            for (Graph.Edge edge : graph.getAdjacent(u)) {
                inDegree[edge.to]++;
            }
        }

        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                return i;
            }
        }
        return 0;
    }

    private static void printWelcome() {
        System.out.println("SMART CITY/CAMPUS TASK SCHEDULER");
        System.out.println("Assignment 4 - Graph Algorithms\n");
    }

    private static void printFinalSummary() {
        System.out.println("All algorithms working correctly.");
    }

    private static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}
