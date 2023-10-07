package org.apache.hadoop.hdfs.server.federation.store.driver;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.Query;
import org.apache.hadoop.hdfs.server.federation.store.records.QueryResult;
import org.apache.hadoop.io.retry.AtMostOnce;
import org.apache.hadoop.io.retry.Idempotent;

import java.io.IOException;
import java.util.List;

// StateStoreDriver 操作数据的api
// 增删改查 Record
@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface StateStoreRecordOperations {

    // 获取所有 Record
    @Idempotent
    <T extends BaseRecord> QueryResult<T> get(Class<T> clazz) throws IOException;

    // 获取一条匹配的 Record
    @Idempotent
    <T extends BaseRecord> T get(Class<T> clazz, Query<T> query) throws IOException;

    // 获取多条匹配的 Record
    // 是个备用功能，因为在 StateStore不支持过滤的时候才会调用此方法 
    @Idempotent
    <T extends BaseRecord> List<T> getMultiple(Class<T> clazz, Query<T> query) throws IOException;

    @AtMostOnce
    <T extends BaseRecord> boolean put(T record, boolean allowUpdate, boolean errorIfExists) throws IOException;

    @AtMostOnce
    <T extends BaseRecord> boolean putAll(List<T> records, boolean allowUpdate, boolean errorIfExists) throws IOException;


    @AtMostOnce
    <T extends BaseRecord> boolean remove(T record) throws IOException;

    @AtMostOnce
    <T extends BaseRecord> int remove(Class<T> clazz, Query<T> query) throws IOException;

    @AtMostOnce
    <T extends BaseRecord> boolean removeAll(Class<T> clazz) throws IOException;

}
