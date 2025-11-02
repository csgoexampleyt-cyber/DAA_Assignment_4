package graph.utility;

import graph.common.Graph;
import graph.common.GraphLoader;

import java.io.File;
import java.util.*;

public class DatasetGenerator {
    private final Random random;

    public DatasetGenerator(long seed) {
        this.random = new Random(seed);
    }

    public Graph generateGraph(int numVertices, double density, boolean hasCycles, int numCycles) {
        Graph graph = new Graph(numVertices);

        String[] taskTypes = {
                "StreetClean", "CameraCheck", "SensorMaint", "RoadRepair",
                "LightFix", "SignCheck", "DataAnalysis", "ReportGen",
                "Inspection", "AlertGen", "StatusUpdate", "Archive",
                "MonitorTraffic", "ReadSensor", "ProcessData", "ValidateData"
        };

        for (int i = 0; i < numVertices; i++) {
            String taskType = taskTypes[random.nextInt(taskTypes.length)];
            graph.setTaskName(i, taskType + "_" + (i + 1));
        }

        int maxPossibleEdges = numVertices * (numVertices - 1);
        int targetEdges = Math.max(numVertices - 1, (int) (maxPossibleEdges * density));

        Set<String> addedEdges = new HashSet<>();

        if (!hasCycles) {
            targetEdges = Math.min(targetEdges, numVertices * (numVertices - 1) / 2);

            for (int i = 1; i < numVertices; i++) {
                int u = random.nextInt(i);
                double weight = 1.0 + random.nextDouble() * 9.0;
                graph.addEdge(u, i, weight);
                addedEdges.add(u + "->" + i);
            }

            while (addedEdges.size() < targetEdges) {
                int u = random.nextInt(numVertices);
                int v = random.nextInt(numVertices);

                if (u < v) {
                    String key = u + "->" + v;
                    if (!addedEdges.contains(key)) {
                        double weight = 1.0 + random.nextDouble() * 9.0;
                        graph.addEdge(u, v, weight);
                        addedEdges.add(key);
                    }
                }
            }
        } else {
            int baseEdges = Math.max(targetEdges - numCycles * 2, numVertices - 1);

            for (int i = 1; i < numVertices; i++) {
                int u = random.nextInt(i);
                double weight = 1.0 + random.nextDouble() * 9.0;
                graph.addEdge(u, i, weight);
                addedEdges.add(u + "->" + i);
            }

            while (addedEdges.size() < baseEdges) {
                int u = random.nextInt(numVertices);
                int v = random.nextInt(numVertices);

                if (u < v) {
                    String key = u + "->" + v;
                    if (!addedEdges.contains(key)) {
                        double weight = 1.0 + random.nextDouble() * 9.0;
                        graph.addEdge(u, v, weight);
                        addedEdges.add(key);
                    }
                }
            }

            int cyclesAdded = 0;
            int attempts = 0;
            while (cyclesAdded < numCycles && attempts < numCycles * 20) {
                int u = random.nextInt(numVertices);
                int v = random.nextInt(numVertices);

                if (u > v) {
                    String key = u + "->" + v;
                    if (!addedEdges.contains(key)) {
                        double weight = 1.0 + random.nextDouble() * 9.0;
                        graph.addEdge(u, v, weight);
                        addedEdges.add(key);
                        cyclesAdded++;
                    }
                }
                attempts++;
            }
        }

        return graph;
    }

    public void generateAllDatasets(String outputDir) throws Exception {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("Dataset Generator");

        System.out.println("Generating Small Datasets (6-10 vertices)");
        generateAndSave(outputDir + "/small_dag_1.json", 6, 0.3, false, 0,
                "Simple DAG with branching");
        generateAndSave(outputDir + "/small_cycle_1.json", 8, 0.35, true, 2,
                "Graph with 2 cycles");
        generateAndSave(outputDir + "/small_sparse_1.json", 10, 0.2, false, 0,
                "Sparse DAG");

        System.out.println();

        System.out.println("Generating Medium Datasets (10-20 vertices)");
        generateAndSave(outputDir + "/medium_dag_1.json", 12, 0.25, false, 0,
                "Multi-branch pipeline");
        generateAndSave(outputDir + "/medium_cycle_1.json", 15, 0.3, true, 3,
                "Complex with 3 cycles");
        generateAndSave(outputDir + "/medium_mixed_1.json", 18, 0.28, true, 4,
                "Mixed structure, 4 cycles");

        System.out.println();

        System.out.println("Generating Large Datasets (20-50 vertices)");
        generateAndSave(outputDir + "/large_sparse_1.json", 25, 0.15, false, 0,
                "Performance test - sparse");
        generateAndSave(outputDir + "/large_dense_1.json", 35, 0.25, true, 5,
                "Performance test - dense");
        generateAndSave(outputDir + "/large_complex_1.json", 45, 0.2, true, 8,
                "Maximum complexity");

        System.out.println(" Successfully generated 9 datasets in " + outputDir);

        printSummary(outputDir);
    }
    private void generateAndSave(String filename, int vertices, double density,
                                 boolean hasCycles, int numCycles, String description) throws Exception {
        Graph graph = generateGraph(vertices, density, hasCycles, numCycles);
        GraphLoader.saveToJSON(graph, filename);

        String basename = new File(filename).getName();
        System.out.println(basename);
        System.out.println("    - " + description);
        System.out.println("    - Vertices: " + vertices + ", Edges: " + graph.countEdges() +
                ", Cycles: " + (hasCycles ? "Yes" : "No"));
    }

    private void printSummary(String outputDir) {
        File dir = new File(outputDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files == null) return;

        System.out.println(" Dataset Summary:");
        System.out.printf("%-30s %8s %8s%n", "File", "Vertices", "Edges");

        int totalVertices = 0;
        int totalEdges = 0;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            try {
                Graph g = GraphLoader.loadFromJSON(file.getPath());
                System.out.printf("%-30s %8d %8d%n",
                        file.getName(), g.getVertices(), g.countEdges());
                totalVertices += g.getVertices();
                totalEdges += g.countEdges();
            } catch (Exception e) {
                System.out.printf("%-30s %8s %8s%n", file.getName(), "ERROR", "ERROR");
            }
        }

        System.out.printf("%-30s %8d %8d%n", "TOTAL", totalVertices, totalEdges);
    }

    public static Graph generateScenario(String scenarioName) {
        Graph graph;

        switch (scenarioName.toLowerCase()) {
            case "sensor_network":
                graph = new Graph(8);
                graph.setTaskName(0, "InitSensors");
                graph.setTaskName(1, "ReadTemp");
                graph.setTaskName(2, "ReadHumidity");
                graph.setTaskName(3, "ProcessData");
                graph.setTaskName(4, "Validate");
                graph.setTaskName(5, "SendAlert");
                graph.setTaskName(6, "Log");
                graph.setTaskName(7, "Archive");

                graph.addEdge(0, 1, 2.0);
                graph.addEdge(0, 2, 2.0);
                graph.addEdge(1, 3, 1.5);
                graph.addEdge(2, 3, 1.5);
                graph.addEdge(3, 4, 3.0);
                graph.addEdge(4, 3, 0.5);
                graph.addEdge(4, 5, 2.0);
                graph.addEdge(5, 6, 1.0);
                graph.addEdge(6, 7, 2.0);
                break;

            case "traffic_management":
                graph = new Graph(10);
                graph.setTaskName(0, "MonitorTraffic");
                graph.setTaskName(1, "DetectIncident");
                graph.setTaskName(2, "AnalyzeImpact");
                graph.setTaskName(3, "RouteOptimization");
                graph.setTaskName(4, "UpdateSignals");
                graph.setTaskName(5, "NotifyDrivers");
                graph.setTaskName(6, "DispatchEmergency");
                graph.setTaskName(7, "UpdateMaps");
                graph.setTaskName(8, "LogEvent");
                graph.setTaskName(9, "ClearIncident");

                graph.addEdge(0, 1, 1.0);
                graph.addEdge(1, 2, 2.0);
                graph.addEdge(2, 3, 3.0);
                graph.addEdge(3, 4, 1.5);
                graph.addEdge(3, 5, 1.0);
                graph.addEdge(1, 6, 4.0);
                graph.addEdge(4, 7, 2.0);
                graph.addEdge(6, 8, 1.0);
                graph.addEdge(7, 8, 1.0);
                graph.addEdge(8, 9, 2.0);
                graph.addEdge(9, 0, 0.5);
                break;

            default:
                graph = new Graph(4);
                graph.setTaskName(0, "Start");
                graph.setTaskName(1, "Process");
                graph.setTaskName(2, "Validate");
                graph.setTaskName(3, "End");

                graph.addEdge(0, 1, 2.0);
                graph.addEdge(1, 2, 3.0);
                graph.addEdge(2, 3, 1.0);
                break;
        }

        return graph;
    }

    public static void main(String[] args) {
        try {
            String outputDir = args.length > 0 ? args[0] : "data";

            DatasetGenerator generator = new DatasetGenerator(42);
            generator.generateAllDatasets(outputDir);

            System.out.println("\n You can now run the scheduler on any generated dataset:");
            System.out.println("   java SmartCityScheduler " + outputDir + "/small_cycle_1.json");
            System.out.println("   java SmartCityScheduler --batch " + outputDir);

        } catch (Exception e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}