package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.hdfs.server.federation.metrics.StateStoreMetrics;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.QueryResult;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 将数据缓存在内存中
 */
public abstract class CachedRecordStore<R extends BaseRecord>
        extends RecordStore<R> implements StateStoreCache {

    private static final Logger LOG =
            LoggerFactory.getLogger(CachedRecordStore.class);


    // 不是强制刷新时，自动刷新缓存 的最小时间间隔
    private static final long MIN_UPDATE_MS = 500;

    // 缓存中 保存的数据
    private List<R> records = new ArrayList<>();

    private long timestamp = -1;

    // 缓存是否初始化
    private boolean initialized = false;

    // 上一次被更新的时间戳
    private long lastUpdate = -1;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    // 是否调用 overrideExpiredRecords，即检查此数据过期的话，同时也把 ZNode里的值删掉
    private final boolean override;


    protected CachedRecordStore(Class<R> clazz, StateStoreDriver driver) {
        this(clazz, driver, false);
    }

    protected CachedRecordStore(
            Class<R> clazz, StateStoreDriver driver, boolean over) {
        super(clazz, driver);

        this.override = over;
    }

    // 检查缓存是否已经初始化
    private void checkCacheAvailable() throws StateStoreUnavailableException {
        if (!this.initialized) {
            throw new StateStoreUnavailableException(
                    "Cached State Store not initialized, " +
                            getRecordClass().getSimpleName() + " records not valid");
        }
    }

    @Override
    public boolean loadCache(boolean force) throws IOException {
        if (force || isUpdateTime()) {
            List<R> newRecords;
            long t;
            try {
                // 从 StateStoreDriver中获取数据
                QueryResult<R> result = getDriver().get(getRecordClass());
                newRecords = result.getRecords();
                t = result.getTimestamp();

                if (this.override) {
                    overrideExpiredRecords(result);
                }
            } catch (IOException e) {
                LOG.error("Cannot get \"{}\" records from the State Store",
                        getRecordClass().getSimpleName());
                this.initialized = false;
                return false;
            }

            // Update cache atomically
            writeLock.lock();
            try {
                this.records.clear();
                this.records.addAll(newRecords);
                this.timestamp = t;
                this.initialized = true;
            } finally {
                writeLock.unlock();
            }

            // 更新指标：records.size()
            StateStoreMetrics metrics = getDriver().getMetrics();
            if (metrics != null) {
                String recordName = getRecordClass().getSimpleName();
                metrics.setCacheSize(recordName, this.records.size());
            }

            lastUpdate = Time.monotonicNow();
        }
        return true;
    }

    // 是否到了 可以更新缓存的时候了
    private boolean isUpdateTime() {
        return Time.monotonicNow() - lastUpdate > MIN_UPDATE_MS;
    }

    // loadCache时，if.override 才会调用 overrideExpiredRecords
    // 对于 QueryResult(Record列表), 判断数据是否过期:
    // 1、如果过期了，则先删除 ZNode，后删缓存
    // 2、如果没过期，则将缓存中的数据强制刷新到 ZNode
    public void overrideExpiredRecords(QueryResult<R> query) throws IOException {

        // 待更新的数据，更新 StateSore中的这些数据
        List<R> commitRecords = new ArrayList<>();

        // 待删除的数据
        List<R> deleteRecords = new ArrayList<>();
        // newRecords.removeAll(deleteRecords)
        List<R> newRecords = query.getRecords();
        long currentDriverTime = query.getTimestamp();

        for (R record : newRecords) {
            if (record.shouldBeDeleted(currentDriverTime)) {
                String recordName = StateStoreUtils.getRecordName(record.getClass());
                if (getDriver().remove(record)) {
                    deleteRecords.add(record);
                    LOG.info("Deleted State Store record {}: {}", recordName, record);
                } else {
                    LOG.warn("Couldn't delete State Store record {}: {}", recordName, record);
                }
            } else if (record.checkExpired(currentDriverTime)) {
                String recordName = StateStoreUtils.getRecordName(record.getClass());
                LOG.info("Override State Store record {}: {}", recordName, record);
                commitRecords.add(record);
            }
        }
        if (commitRecords.size() > 0) {
            getDriver().putAll(commitRecords, true, false);
        }
        if (deleteRecords.size() > 0) {
            newRecords.removeAll(deleteRecords);
        }
    }

    // 针对的是某个数据
    public void overrideExpiredRecord(R record) throws IOException {
        List<R> newRecords = new ArrayList<>();
        newRecords.add(record);
        long time = getDriver().getTime();
        QueryResult<R> query = new QueryResult<>(newRecords, time);
        overrideExpiredRecords(query);
    }

    // 获取缓存中 所有的数据（拷贝的副本）
    public List<R> getCachedRecords() throws StateStoreUnavailableException {
        checkCacheAvailable();
        List<R> ret = new LinkedList<R>();
        this.readLock.lock();
        try {
            ret.addAll(this.records);
        } finally {
            this.readLock.unlock();
        }
        return ret;
    }

    // 获取缓存中 所有的数据 以及对应的时间戳（拷贝的副本）
    protected QueryResult<R> getCachedRecordsAndTimeStamp()
            throws StateStoreUnavailableException {
        checkCacheAvailable();

        this.readLock.lock();
        try {
            return new QueryResult<R>(this.records, this.timestamp);
        } finally {
            this.readLock.unlock();
        }
    }
}
