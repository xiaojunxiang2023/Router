package org.apache.hadoop.hdfs.server.federation.metrics;

// 当 metrics被禁用时，才采取此指标
public class NullStateStoreMetrics extends StateStoreMetrics {
    public void addRead(long latency) {
    }

    public long getReadOps() {
        return -1;
    }

    public double getReadAvg() {
        return -1;
    }

    public void addWrite(long latency) {
    }

    public long getWriteOps() {
        return -1;
    }

    public double getWriteAvg() {
        return -1;
    }

    public void addFailure(long latency) {
    }

    public long getFailureOps() {
        return -1;
    }

    public double getFailureAvg() {
        return -1;
    }

    public void addRemove(long latency) {
    }

    public long getRemoveOps() {
        return -1;
    }

    public double getRemoveAvg() {
        return -1;
    }

    public void setCacheSize(String name, int size) {
    }

    public void reset() {
    }

    public void shutdown() {
    }
}
