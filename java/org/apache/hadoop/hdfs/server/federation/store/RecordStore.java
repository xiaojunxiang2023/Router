package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

// 相当于是一条存储数据
@InterfaceAudience.Private
@InterfaceStability.Evolving
public abstract class RecordStore<R extends BaseRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(RecordStore.class);


    private final Class<R> recordClass;
    private final StateStoreDriver driver;


    protected RecordStore(Class<R> clazz, StateStoreDriver stateStoreDriver) {
        this.recordClass = clazz;
        this.driver = stateStoreDriver;
    }

    public Class<R> getRecordClass() {
        return this.recordClass;
    }

    protected StateStoreDriver getDriver() {
        return this.driver;
    }

    // 根据 .class 类型，来去反射创建对应的 StateStore 实现类
    public static <T extends RecordStore<?>> T newInstance(
            final Class<T> clazz, final StateStoreDriver driver) {

        try {
            Constructor<T> constructor = clazz.getConstructor(StateStoreDriver.class);
            T recordStore = constructor.newInstance(driver);
            return recordStore;
        } catch (Exception e) {
            LOG.error("Cannot create new instance for " + clazz, e);
            return null;
        }
    }
}
