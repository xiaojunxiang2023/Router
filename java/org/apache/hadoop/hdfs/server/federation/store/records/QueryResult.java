package org.apache.hadoop.hdfs.server.federation.store.records;

import java.util.List;

// 一组 Record数据，并包含总体的时间戳
public class QueryResult<T extends BaseRecord> {

    private final List<T> records;
    private final long timestamp;

    public QueryResult(final List<T> recs, final long time) {
        this.records = recs;
        this.timestamp = time;
    }

    public List<T> getRecords() {
        return this.records;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
