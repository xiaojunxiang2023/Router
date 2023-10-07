package org.apache.hadoop.hdfs.server.federation.store.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.metrics.StateStoreMetrics;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreUnavailableException;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreUtils;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Collection;

public abstract class StateStoreDriver implements StateStoreRecordOperations {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreDriver.class);


    private Configuration conf;
    private String identifier;
    private StateStoreMetrics metrics;


    public boolean init(final Configuration config, final String id,
                        final Collection<Class<? extends BaseRecord>> records,
                        final StateStoreMetrics stateStoreMetrics) {

        this.conf = config;
        this.identifier = id;
        this.metrics = stateStoreMetrics;

        if (this.identifier == null) {
            LOG.warn("The identifier for the State Store connection is not set");
        }

        boolean success = initDriver();
        if (!success) {
            LOG.error("Cannot initialize driver for {}", getDriverName());
            return false;
        }

        for (Class<? extends BaseRecord> cls : records) {
            String recordString = StateStoreUtils.getRecordName(cls);
            if (!initRecordStorage(recordString, cls)) {
                LOG.error("Cannot initialize record store for {}", cls.getSimpleName());
                return false;
            }
        }
        return true;
    }

    protected Configuration getConf() {
        return this.conf;
    }

    // 获取正在运行的任务的唯一标识符, 一般是 router 地址
    public String getIdentifier() {
        return this.identifier;
    }

    public StateStoreMetrics getMetrics() {
        return this.metrics;
    }

    // 初始化 Driver
    public abstract boolean initDriver();

    // 初始化 RecordStorage
    public abstract <T extends BaseRecord> boolean initRecordStorage(
            String className, Class<T> clazz);

    public abstract boolean isDriverReady();

    public void verifyDriverReady() throws StateStoreUnavailableException {
        if (!isDriverReady()) {
            String driverName = getDriverName();
            String hostname = getHostname();
            throw new StateStoreUnavailableException("State Store driver " +
                    driverName + " in " + hostname + " is not ready.");
        }
    }

    public abstract void close() throws Exception;

    public long getTime() {
        return Time.now();
    }

    private String getDriverName() {
        return this.getClass().getSimpleName();
    }

    private String getHostname() {
        String hostname = "Unknown";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LOG.error("Cannot get local address", e);
        }
        return hostname;
    }
}
