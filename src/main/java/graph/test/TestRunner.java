package graph.test;

import graph.common.BasicMetrics;
import graph.common.Graph;
import graph.common.Metrics;
import graph.dagsp.DAGShortestPath;
import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failedTestNames = new ArrayList<>();
    private static List<TestResult> testResults = new ArrayList<>();

    static class TestResult {
        String category;
        String name;
        String status;
        String errorMessage;
        long executionTimeNanos;
        int vertices;
        int edges;
        String operationCounters;
        int sccs;
        boolean hasCycles;

        TestResult(String category, String name, String status, String errorMessage,
                   long executionTimeNanos, int vertices, int edges,
                   String operationCounters, int sccs, boolean hasCycles) {
            this.category = category;
            this.name = name;
            this.status = status;
            this.errorMessage = errorMessage;
            this.executionTimeNanos = executionTimeNanos;
            this.vertices = vertices;
            this.edges = edges;
            this.operationCounters = operationCounters;
            this.sccs = sccs;
            this.hasCycles = hasCycles;
        }
    }

    public static void main(String[] args) {
        printHeader("Assignment 4 - Tests");

        System.out.println("Running all algorithm tests.\n");

        runSCCTests();
        runTopologicalSortTests();
        runShortestPathTests();
        runIntegrationTests();
        runEdgeCaseTests();

        printSummary();

        String csvFilename = args.length > 0 ? args[0] : "test_results.csv";
        saveResultsToCSV(csvFilename);
    }

    private static void runSCCTests() {
        printTestCategory("Strongly Connected Components (Tarjan's Algorithm)");
        String category = "SCC";

        test(category, "SCC - Simple Cycle (3 vertices)", () -> {
            Graph g = new Graph(3);
            g.setTaskName(0, "A");
            g.setTaskName(1, "B");
            g.setTaskName(2, "C");
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 0);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertEquals(1, components.size(), "Should have 1 SCC");
            assertEquals(3, components.get(0).size(), "SCC should contain 3 vertices");

            return new TestMetrics(3, 3, metrics, components.size(), true);
        });

        test(category, "SCC - Pure DAG (no cycles)", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 3);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertEquals(4, components.size(), "Should have 4 SCCs (one per vertex)");

            return new TestMetrics(4, 3, metrics, components.size(), false);
        });

        test(category, "SCC - Multiple Components", () -> {
            Graph g = new Graph(6);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 0);
            g.addEdge(2, 3);
            g.addEdge(3, 4);
            g.addEdge(4, 3);
            g.addEdge(4, 5);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertEquals(3, components.size(), "Should have 3 SCCs");

            return new TestMetrics(6, 7, metrics, components.size(), true);
        });

        test(category, "SCC - Single Vertex", () -> {
            Graph g = new Graph(1);
            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertEquals(1, components.size(), "Should have 1 SCC");
            assertEquals(1, components.get(0).size(), "SCC should have 1 vertex");

            return new TestMetrics(1, 0, metrics, components.size(), false);
        });

        test(category, "SCC - Condensation Graph", () -> {
            Graph g = new Graph(5);
            g.addEdge(0, 1);
            g.addEdge(1, 0);
            g.addEdge(1, 2);
            g.addEdge(2, 3);
            g.addEdge(3, 4);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();
            Graph condensation = scc.buildCondensationGraph(components);

            assertEquals(4, condensation.getVertices(), "Condensation should have 4 vertices");
            assertTrue(condensation.countEdges() >= 3, "Should have at least 3 edges");

            return new TestMetrics(5, 5, metrics, components.size(), true);
        });
    }

    private static void runTopologicalSortTests() {
        printTestCategory("Topological Sorting (Kahn's Algorithm)");
        String category = "Topological Sort";

        test(category, "Topo - Simple Linear DAG", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 3);

            BasicMetrics metrics = new BasicMetrics();
            TopologicalSort topo = new TopologicalSort(g, metrics);
            List<Integer> order = topo.sort();

            assertEquals(4, order.size(), "Should have all 4 vertices");
            assertTrue(order.indexOf(0) < order.indexOf(1), "0 before 1");
            assertTrue(order.indexOf(1) < order.indexOf(2), "1 before 2");
            assertTrue(order.indexOf(2) < order.indexOf(3), "2 before 3");

            return new TestMetrics(4, 3, metrics, 4, false);
        });

        test(category, "Topo - Diamond Graph", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1);
            g.addEdge(0, 2);
            g.addEdge(1, 3);
            g.addEdge(2, 3);

            BasicMetrics metrics = new BasicMetrics();
            TopologicalSort topo = new TopologicalSort(g, metrics);
            List<Integer> order = topo.sort();

            assertEquals(4, order.size(), "Should have all 4 vertices");
            assertTrue(order.indexOf(0) < order.indexOf(1), "0 before 1");
            assertTrue(order.indexOf(0) < order.indexOf(2), "0 before 2");
            assertTrue(order.indexOf(1) < order.indexOf(3), "1 before 3");
            assertTrue(order.indexOf(2) < order.indexOf(3), "2 before 3");

            return new TestMetrics(4, 4, metrics, 4, false);
        });

        test(category, "Topo - Cycle Detection", () -> {
            Graph g = new Graph(3);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 0);

            BasicMetrics metrics = new BasicMetrics();
            TopologicalSort topo = new TopologicalSort(g, metrics);
            List<Integer> order = topo.sort();

            assertEquals(0, order.size(), "Should return empty for cyclic graph");

            return new TestMetrics(3, 3, metrics, 0, true);
        });

        test(category, "Topo - Multiple Sources", () -> {
            Graph g = new Graph(5);
            g.addEdge(0, 2);
            g.addEdge(1, 2);
            g.addEdge(2, 3);
            g.addEdge(2, 4);

            BasicMetrics metrics = new BasicMetrics();
            TopologicalSort topo = new TopologicalSort(g, metrics);
            List<Integer> order = topo.sort();

            assertEquals(5, order.size(), "Should have all 5 vertices");
            assertTrue(order.indexOf(2) < order.indexOf(3), "2 before 3");
            assertTrue(order.indexOf(2) < order.indexOf(4), "2 before 4");

            return new TestMetrics(5, 4, metrics, 5, false);
        });
    }

    private static void runShortestPathTests() {
        printTestCategory("Shortest and Longest Paths on DAGs");
        String category = "Path Algorithms";

        test(category, "Path - Simple Shortest Path", () -> {
            Graph g = new Graph(3);
            g.addEdge(0, 1, 2.0);
            g.addEdge(1, 2, 3.0);

            BasicMetrics metrics = new BasicMetrics();
            DAGShortestPath dagSP = new DAGShortestPath(g, metrics);
            DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

            assertTrue(result.isValid, "Should be valid DAG");
            assertEquals(0.0, result.dist[0], 0.001, "Distance to source = 0");
            assertEquals(2.0, result.dist[1], 0.001, "Distance to 1 = 2");
            assertEquals(5.0, result.dist[2], 0.001, "Distance to 2 = 5");

            return new TestMetrics(3, 2, metrics, 3, false);
        });

        test(category, "Path - Multiple Paths", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1, 1.0);
            g.addEdge(0, 2, 5.0);
            g.addEdge(1, 3, 1.0);
            g.addEdge(2, 3, 1.0);

            BasicMetrics metrics = new BasicMetrics();
            DAGShortestPath dagSP = new DAGShortestPath(g, metrics);
            DAGShortestPath.PathResult result = dagSP.shortestPaths(0);

            assertEquals(2.0, result.dist[3], 0.001, "Shortest path = 2");

            return new TestMetrics(4, 4, metrics, 4, false);
        });

        test(category, "Path - Longest Path", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1, 2.0);
            g.addEdge(0, 2, 1.0);
            g.addEdge(1, 3, 1.0);
            g.addEdge(2, 3, 4.0);

            BasicMetrics metrics = new BasicMetrics();
            DAGShortestPath dagSP = new DAGShortestPath(g, metrics);
            DAGShortestPath.PathResult result = dagSP.longestPaths(0);

            assertEquals(5.0, result.dist[3], 0.001, "Longest path = 5");

            return new TestMetrics(4, 4, metrics, 4, false);
        });

        test(category, "Path - Critical Path Finding", () -> {
            Graph g = new Graph(5);
            g.addEdge(0, 1, 3.0);
            g.addEdge(0, 2, 2.0);
            g.addEdge(1, 3, 4.0);
            g.addEdge(2, 3, 1.0);
            g.addEdge(3, 4, 2.0);

            BasicMetrics metrics = new BasicMetrics();
            DAGShortestPath dagSP = new DAGShortestPath(g, metrics);
            DAGShortestPath.CriticalPathResult result = dagSP.findCriticalPath();

            assertEquals(9.0, result.length, 0.001, "Critical path length = 9");
            assertFalse(result.path.isEmpty(), "Path should not be empty");

            return new TestMetrics(5, 5, metrics, 5, false);
        });

        test(category, "Path - Path Reconstruction", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1, 1.0);
            g.addEdge(1, 2, 1.0);
            g.addEdge(2, 3, 1.0);

            BasicMetrics metrics = new BasicMetrics();
            DAGShortestPath dagSP = new DAGShortestPath(g, metrics);
            DAGShortestPath.PathResult result = dagSP.shortestPaths(0);
            List<Integer> path = result.getPath(0, 3);

            assertEquals(4, path.size(), "Path should have 4 vertices");
            assertEquals(Integer.valueOf(0), path.get(0), "Start at 0");
            assertEquals(Integer.valueOf(3), path.get(3), "End at 3");

            return new TestMetrics(4, 3, metrics, 4, false);
        });
    }

    private static void runIntegrationTests() {
        printTestCategory("Integration Tests");
        String category = "Integration";

        test(category, "Integration - Complete Analysis", () -> {
            Graph g = new Graph(6);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 0);
            g.addEdge(2, 3);
            g.addEdge(3, 4);
            g.addEdge(4, 5);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            TarjanSCC.SCCResult sccResult = scc.getResults();

            assertTrue(sccResult.sccs.size() >= 3, "Should have at least 3 SCCs");

            TopologicalSort topo = new TopologicalSort(sccResult.condensation, new BasicMetrics());
            List<Integer> order = topo.sort();

            assertFalse(order.isEmpty(), "Should have valid topo order");

            return new TestMetrics(6, 6, metrics, sccResult.sccs.size(), true);
        });

        test(category, "Integration - Metrics Tracking", () -> {
            Graph g = new Graph(5);
            g.addEdge(0, 1);
            g.addEdge(1, 2);
            g.addEdge(2, 3);
            g.addEdge(3, 4);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            scc.findSCCs();

            assertTrue(metrics.getCounter("dfs_visits") > 0, "Should track DFS visits");
            assertTrue(metrics.getElapsedTimeNanos() > 0, "Should track time");

            return new TestMetrics(5, 4, metrics, 5, false);
        });
    }

    private static void runEdgeCaseTests() {
        printTestCategory("Edge Cases");
        String category = "Edge Cases";

        test(category, "Edge - Empty Graph", () -> {
            Graph g = new Graph(0);
            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertEquals(0, components.size(), "Empty graph has 0 SCCs");

            return new TestMetrics(0, 0, metrics, 0, false);
        });

        test(category, "Edge - Disconnected Components", () -> {
            Graph g = new Graph(4);
            g.addEdge(0, 1);
            g.addEdge(2, 3);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertEquals(4, components.size(), "Should have 4 separate SCCs");

            return new TestMetrics(4, 2, metrics, 4, false);
        });

        test(category, "Edge - Self Loop", () -> {
            Graph g = new Graph(2);
            g.addEdge(0, 0);
            g.addEdge(0, 1);

            BasicMetrics metrics = new BasicMetrics();
            TarjanSCC scc = new TarjanSCC(g, metrics);
            List<List<Integer>> components = scc.findSCCs();

            assertTrue(components.size() >= 1, "Should handle self loops");

            return new TestMetrics(2, 2, metrics, components.size(), true);
        });
    }

    static class TestMetrics {
        int vertices;
        int edges;
        Metrics metrics;
        int sccs;
        boolean hasCycles;

        TestMetrics(int vertices, int edges, Metrics metrics, int sccs, boolean hasCycles) {
            this.vertices = vertices;
            this.edges = edges;
            this.metrics = metrics;
            this.sccs = sccs;
            this.hasCycles = hasCycles;
        }
    }

    private static void test(String category, String name, TestCaseWithMetrics testCase) {
        totalTests++;
        long startTime = System.nanoTime();
        String status;
        String errorMessage = "";
        TestMetrics testMetrics = null;

        try {
            testMetrics = testCase.run();
            passedTests++;
            status = "PASS";
            System.out.println("  PASSED " + name);
        } catch (AssertionError e) {
            failedTests++;
            failedTestNames.add(name);
            status = "FAIL";
            errorMessage = e.getMessage();
            System.out.println("  FAILED " + name);
            System.out.println("    Error: " + errorMessage);
        } catch (Exception e) {
            failedTests++;
            failedTestNames.add(name);
            status = "ERROR";
            errorMessage = e.getMessage();
            System.out.println("  FAILED " + name);
            System.out.println("    Exception: " + errorMessage);
        }

        long executionTime = System.nanoTime() - startTime;

        int vertices = testMetrics != null ? testMetrics.vertices : 0;
        int edges = testMetrics != null ? testMetrics.edges : 0;
        int sccs = testMetrics != null ? testMetrics.sccs : 0;
        boolean hasCycles = testMetrics != null && testMetrics.hasCycles;

        String operationCounters = "";
        if (testMetrics != null && testMetrics.metrics != null) {
            StringBuilder sb = new StringBuilder();
            Metrics m = testMetrics.metrics;

            if (m.getCounter("dfs_visits") > 0) {
                sb.append("dfs_visits=").append(m.getCounter("dfs_visits")).append("; ");
            }
            if (m.getCounter("edge_traversals") > 0) {
                sb.append("edge_traversals=").append(m.getCounter("edge_traversals")).append("; ");
            }
            if (m.getCounter("stack_pops") > 0) {
                sb.append("stack_pops=").append(m.getCounter("stack_pops")).append("; ");
            }
            if (m.getCounter("queue_pushes") > 0) {
                sb.append("queue_pushes=").append(m.getCounter("queue_pushes")).append("; ");
            }
            if (m.getCounter("queue_pops") > 0) {
                sb.append("queue_pops=").append(m.getCounter("queue_pops")).append("; ");
            }
            if (m.getCounter("relaxations") > 0) {
                sb.append("relaxations=").append(m.getCounter("relaxations")).append("; ");
            }

            operationCounters = sb.toString().trim();
        }

        testResults.add(new TestResult(category, name, status, errorMessage,
                executionTime, vertices, edges,
                operationCounters, sccs, hasCycles));
    }

    private static void saveResultsToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Category,Test Name,Status,Vertices (n),Edges (m),SCCs,Has Cycles,Execution Time (ms),Operation Counters,Error Message");

            for (TestResult result : testResults) {
                String escapedError = result.errorMessage.replace("\"", "\"\"");
                String escapedCounters = result.operationCounters.replace("\"", "\"\"");
                double executionTimeMs = result.executionTimeNanos / 1_000_000.0;

                writer.printf("\"%s\",\"%s\",\"%s\",%d,%d,%d,%s,%.3f,\"%s\",\"%s\"%n",
                        result.category,
                        result.name,
                        result.status,
                        result.vertices,
                        result.edges,
                        result.sccs,
                        result.hasCycles ? "Yes" : "No",
                        executionTimeMs,
                        escapedCounters,
                        escapedError);
            }

            writer.println();
            writer.println("SUMMARY");
            writer.printf("Total Tests,%d%n", totalTests);
            writer.printf("Passed,%d%n", passedTests);
            writer.printf("Failed,%d%n", failedTests);
            writer.printf("Success Rate,%.1f%%%n",
                    (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));

            System.out.println("\nTest results saved to: " + filename);

        } catch (Exception e) {
            System.err.println("\nError saving CSV file: " + e.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void printHeader(String title) {
        System.out.println(centerText(title, 70));
    }

    private static void printTestCategory(String category) {
        System.out.println(category);
    }

    private static void printSummary() {
        System.out.println(centerText("TEST SUMMARY", 70));
        System.out.printf("Total Tests:   %d%n", totalTests);
        System.out.printf("Passed:        %d%n", passedTests);
        System.out.printf("Failed:        %d%n", failedTests);
        System.out.printf("Success Rate:  %.1f%%%n",
                (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));

        if (failedTests > 0) {
            System.out.println("\nFailed Tests:");
            for (String testName : failedTestNames) {
                System.out.println("  - " + testName);
            }
        } else {
            System.out.println("\nAll tests passed successfully!");
        }
    }

    private static String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text +
                " ".repeat(Math.max(0, width - text.length() - padding));
    }

    @FunctionalInterface
    interface TestCaseWithMetrics {
        TestMetrics run() throws Exception;
    }
}