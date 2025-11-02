package graph.common;

import java.util.HashMap;
import java.util.Map;

public class BasicMetrics implements Metrics {
    private long startTime;
    private long endTime;
    private final Map<String, Long> counters;

    public BasicMetrics() {
        this.counters = new HashMap<>();
        reset();
    }

    @Override
    public void startTiming() {
        startTime = System.nanoTime();
    }

    @Override
    public void stopTiming() {
        endTime = System.nanoTime();
    }

    @Override
    public long getElapsedTimeNanos() {
        return endTime - startTime;
    }

    @Override
    public double getElapsedTimeMillis() {
        return getElapsedTimeNanos() / 1_000_000.0;
    }

    @Override
    public void incrementCounter(String operation) {
        counters.put(operation, counters.getOrDefault(operation, 0L) + 1);
    }

    @Override
    public long getCounter(String operation) {
        return counters.getOrDefault(operation, 0L);
    }

    @Override
    public void reset() {
        startTime = 0;
        endTime = 0;
        counters.clear();
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Execution Time: ").append(String.format("%.3f", getElapsedTimeMillis())).append(" ms\n");
        sb.append("Operation Counters:\n");
        counters.forEach((op, count) ->
                sb.append("  ").append(op).append(": ").append(count).append("\n"));
        return sb.toString();
    }
}
