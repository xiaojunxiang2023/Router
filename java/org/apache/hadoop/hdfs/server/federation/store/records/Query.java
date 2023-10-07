package org.apache.hadoop.hdfs.server.federation.store.records;

public class Query<T extends BaseRecord> {

    private final T partial;

    public Query(final T part) {
        this.partial = part;
    }

    public T getPartial() {
        return this.partial;
    }

    // 是否匹配 (equals)
    public boolean matches(T other) {
        if (this.partial == null) {
            return false;
        }
        return this.partial.like(other);
    }

    @Override
    public String toString() {
        return "Checking: " + this.partial;
    }
}
