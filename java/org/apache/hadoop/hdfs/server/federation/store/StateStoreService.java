package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.metrics.NullStateStoreMetrics;
import org.apache.hadoop.hdfs.server.federation.metrics.StateStoreMBean;
import org.apache.hadoop.hdfs.server.federation.metrics.StateStoreMetrics;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.impl.DisabledNameserviceStoreImpl;
import org.apache.hadoop.hdfs.server.federation.store.impl.MembershipStoreImpl;
import org.apache.hadoop.hdfs.server.federation.store.impl.MountTableStoreImpl;
import org.apache.hadoop.hdfs.server.federation.store.impl.RouterStoreImpl;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;
import org.apache.hadoop.metrics2.MetricsException;
import org.apache.hadoop.metrics2.util.MBeans;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

// 1、RecordStore [MembershipStoreImpl，MountTableStoreImpl] 有对 Record基本的管理能力
//      底层持有 Driver的引用，调用 Driver去增删改查 ZNode。
// 2、StateStoreService (state store) 对 Driver进行初始化，和注册 RecordStore
// 3、和持有 monitorService 与 cacheUpdater 这两个 Service 【用来定期操作 Driver】
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class StateStoreService extends CompositeService {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreService.class);


    private Configuration conf;
    private String identifier;
    private StateStoreDriver driver;

    // 定期性地检查与 StateStore的连接的 Service 
    private StateStoreConnectionMonitorService monitorService;
    // 定期更新缓存的 Service
    private StateStoreCacheUpdateService cacheUpdater;

    private StateStoreMetrics metrics;

    // Map, 支持的类型： Record.class -> RecordStore
    private final Map<Class<? extends BaseRecord>, RecordStore<? extends BaseRecord>> recordStores;
    
    // refreshCaches() 调用，为之更新 lastUpdateTime
    private long cacheLastUpdateTime;
    private final List<StateStoreCache> cachesToUpdateInternal;
    private final List<StateStoreCache> cachesToUpdateExternal;


    public StateStoreService() {
        super(StateStoreService.class.getName());

        this.recordStores = new HashMap<>();
        this.cachesToUpdateInternal = new ArrayList<>();
        this.cachesToUpdateExternal = new ArrayList<>();
    }

    // 服务初始化
    @Override
    protected void serviceInit(Configuration config) throws Exception {
        this.conf = config;

        // 1、创建 StateStoreDriver
        Class<? extends StateStoreDriver> driverClass = this.conf.getClass(
                RBFConfigKeys.FEDERATION_STORE_DRIVER_CLASS,
                RBFConfigKeys.FEDERATION_STORE_DRIVER_CLASS_DEFAULT,
                StateStoreDriver.class);
        this.driver = ReflectionUtils.newInstance(driverClass, this.conf);

        if (this.driver == null) {
            throw new IOException("Cannot create driver for the State Store");
        }

        // 添加支持的 RecordStore
        addRecordStore(MembershipStoreImpl.class);
        addRecordStore(MountTableStoreImpl.class);
        addRecordStore(RouterStoreImpl.class);
        addRecordStore(DisabledNameserviceStoreImpl.class);

        // 2、monitorService Service
        this.monitorService = new StateStoreConnectionMonitorService(this);
        this.addService(monitorService);

        MembershipState.setExpirationMs(conf.getTimeDuration(
                RBFConfigKeys.FEDERATION_STORE_MEMBERSHIP_EXPIRATION_MS,
                RBFConfigKeys.FEDERATION_STORE_MEMBERSHIP_EXPIRATION_MS_DEFAULT,
                TimeUnit.MILLISECONDS));
        MembershipState.setDeletionMs(conf.getTimeDuration(
                RBFConfigKeys.FEDERATION_STORE_MEMBERSHIP_EXPIRATION_DELETION_MS,
                RBFConfigKeys
                        .FEDERATION_STORE_MEMBERSHIP_EXPIRATION_DELETION_MS_DEFAULT,
                TimeUnit.MILLISECONDS));
        RouterState.setExpirationMs(conf.getTimeDuration(
                RBFConfigKeys.FEDERATION_STORE_ROUTER_EXPIRATION_MS,
                RBFConfigKeys.FEDERATION_STORE_ROUTER_EXPIRATION_MS_DEFAULT,
                TimeUnit.MILLISECONDS));
        RouterState.setDeletionMs(conf.getTimeDuration(
                RBFConfigKeys.FEDERATION_STORE_ROUTER_EXPIRATION_DELETION_MS,
                RBFConfigKeys.FEDERATION_STORE_ROUTER_EXPIRATION_DELETION_MS_DEFAULT,
                TimeUnit.MILLISECONDS));

        // 3、cacheUpdater Service
        this.cacheUpdater = new StateStoreCacheUpdateService(this);
        this.addService(this.cacheUpdater);

        if (conf.getBoolean(RBFConfigKeys.DFS_ROUTER_METRICS_ENABLE,
                RBFConfigKeys.DFS_ROUTER_METRICS_ENABLE_DEFAULT)) {
            this.metrics = StateStoreMetrics.create(conf);
            try {
                StandardMBean bean = new StandardMBean(metrics, StateStoreMBean.class);
                ObjectName registeredObject =
                        MBeans.register("Router", "StateStore", bean);
                LOG.info("Registered StateStoreMBean: {}", registeredObject);
            } catch (NotCompliantMBeanException e) {
                throw new RuntimeException("Bad StateStoreMBean setup", e);
            } catch (MetricsException e) {
                LOG.error("Failed to register State Store bean {}", e.getMessage());
            }
        } else {
            LOG.info("State Store metrics not enabled");
            this.metrics = new NullStateStoreMetrics();
        }

        super.serviceInit(this.conf);
    }

    @Override
    protected void serviceStart() throws Exception {
        loadDriver();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        closeDriver();
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        super.serviceStop();
    }

    private <T extends RecordStore<?>> void addRecordStore(final Class<T> clazz) {

        assert this.getServiceState() == STATE.INITED : "Cannot add record to the State Store once started";

        T recordStore = RecordStore.newInstance(clazz, this.getDriver());
        Class<? extends BaseRecord> recordClass = recordStore.getRecordClass();
        this.recordStores.put(recordClass, recordStore);

        // 订阅内部缓存 以致于可以被定期更新
        if (recordStore instanceof StateStoreCache) {
            StateStoreCache cachedRecordStore = (StateStoreCache) recordStore;
            this.cachesToUpdateInternal.add(cachedRecordStore);
        }
    }

    // 根据 RecordStore.class 取得对应的实例
    public <T extends RecordStore<?>> T getRegisteredRecordStore(
            final Class<T> recordStoreClass) {
        for (RecordStore<? extends BaseRecord> recordStore : this.recordStores.values()) {
            if (recordStoreClass.isInstance(recordStore)) {
                return (T) recordStore;
            }
        }
        return null;
    }

    public Collection<Class<? extends BaseRecord>> getSupportedRecords() {
        return this.recordStores.keySet();
    }

    // 加载 Driver
    public void loadDriver() {
        synchronized (this.driver) {
            if (!isDriverReady()) {
                String driverName = this.driver.getClass().getSimpleName();
                if (this.driver.init(conf, getIdentifier(), getSupportedRecords(), metrics)) {
                    LOG.info("Connection to the State Store driver {} is open and ready",
                            driverName);
                    this.refreshCaches();
                } else {
                    LOG.error("Cannot initialize State Store driver {}", driverName);
                }
            }
        }
    }

    public boolean isDriverReady() {
        return this.driver.isDriverReady();
    }

    @VisibleForTesting
    public void closeDriver() throws Exception {
        if (this.driver != null) {
            this.driver.close();
        }
    }

    public StateStoreDriver getDriver() {
        return this.driver;
    }

    // 获取 StateStore的唯一标识符，一般是 Router地址
    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String id) {
        this.identifier = id;
    }


    public long getCacheUpdateTime() {
        return this.cacheLastUpdateTime;
    }

    @VisibleForTesting
    public void stopCacheUpdateService() {
        if (this.cacheUpdater != null) {
            this.cacheUpdater.stop();
            removeService(this.cacheUpdater);
            this.cacheUpdater = null;
        }
    }

    // 注册一个外部缓存, Resolve会使用这个进行订阅
    public void registerCacheExternal(StateStoreCache client) {
        this.cachesToUpdateExternal.add(client);
    }

    // 从 StateStore 中刷新缓存
    public void refreshCaches() {
        refreshCaches(false);
    }

    public void refreshCaches(boolean force) {
        boolean success = true;
        if (isDriverReady()) {
            List<StateStoreCache> cachesToUpdate = new LinkedList<>();
            cachesToUpdate.addAll(cachesToUpdateInternal);
            cachesToUpdate.addAll(cachesToUpdateExternal);
            for (StateStoreCache cachedStore : cachesToUpdate) {
                String cacheName = cachedStore.getClass().getSimpleName();
                boolean result = false;
                try {
                    result = cachedStore.loadCache(force);
                } catch (IOException e) {
                    LOG.error("Error updating cache for {}", cacheName, e);
                }
                if (!result) {
                    success = false;
                    LOG.error("Cache update failed for cache {}", cacheName);
                }
            }
        } else {
            success = false;
            LOG.info("Skipping State Store cache update, driver is not ready.");
        }
        if (success) {
            // 用的是自己的时间戳，不是 Driver的时间
            this.cacheLastUpdateTime = Time.now();
        }
    }

    // 加载指定类型的 Record
    public boolean loadCache(final Class<?> clazz) throws IOException {
        return loadCache(clazz, false);
    }

    public boolean loadCache(Class<?> clazz, boolean force) throws IOException {
        List<StateStoreCache> cachesToUpdate =
                new LinkedList<StateStoreCache>();
        cachesToUpdate.addAll(this.cachesToUpdateInternal);
        cachesToUpdate.addAll(this.cachesToUpdateExternal);
        for (StateStoreCache cachedStore : cachesToUpdate) {
            if (clazz.isInstance(cachedStore)) {
                return cachedStore.loadCache(force);
            }
        }
        throw new IOException("Registered cache was not found for " + clazz);
    }

    public StateStoreMetrics getMetrics() {
        return metrics;
    }

}
