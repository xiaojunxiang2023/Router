package org.apache.hadoop.hdfs.server.federation.store.driver.impl;

import org.apache.hadoop.hdfs.server.federation.store.StateStoreUtils;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.Query;
import org.apache.hadoop.hdfs.server.federation.store.records.QueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.hadoop.hdfs.server.federation.store.StateStoreUtils.filterMultiple;

/**
 * Base implementation of a State Store driver. It contains default
 * implementations for the optional functions. These implementations use an
 * uncached read/write all algorithm for all changes. In most cases it is
 * recommended to override the optional functions.
 * <p>
 * Drivers may optionally override additional routines for performance
 * optimization, such as custom get/put/remove queries, depending on the
 * capabilities of the data store.
 */
public abstract class StateStoreBaseImpl extends StateStoreDriver {

    @Override
    public <T extends BaseRecord> T get(
            Class<T> clazz, Query<T> query) throws IOException {
        List<T> records = getMultiple(clazz, query);
        if (records.size() > 1) {
            throw new IOException("Found more than one object in collection");
        } else if (records.size() == 1) {
            return records.get(0);
        } else {
            return null;
        }
    }

    @Override
    public <T extends BaseRecord> List<T> getMultiple(
            Class<T> clazz, Query<T> query) throws IOException {
        QueryResult<T> result = get(clazz);
        List<T> records = result.getRecords();
        return filterMultiple(query, records);
    }

    @Override
    public <T extends BaseRecord> boolean put(T record, boolean allowUpdate, boolean errorIfExists) throws IOException {
        List<T> singletonList = new ArrayList<>();
        singletonList.add(record);
        return putAll(singletonList, allowUpdate, errorIfExists);
    }

    @Override
    public <T extends BaseRecord> boolean remove(T record) throws IOException {
        final Query<T> query = new Query<T>(record);
        Class<? extends BaseRecord> clazz = record.getClass();
        @SuppressWarnings("unchecked")
        Class<T> recordClass = (Class<T>) StateStoreUtils.getRecordClass(clazz);
        return remove(recordClass, query) == 1;
    }
}