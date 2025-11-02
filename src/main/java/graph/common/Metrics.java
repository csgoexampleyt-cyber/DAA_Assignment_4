package graph.common;

public interface Metrics {
    void startTiming();
    void stopTiming();
    long getElapsedTimeNanos();
    double getElapsedTimeMillis();
    void incrementCounter(String operation);
    long getCounter(String operation);
    void reset();
    String getSummary();
}