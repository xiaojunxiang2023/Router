package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.store.*;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RouterHeartbeatRequest;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RouterHeartbeatResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;
import org.apache.hadoop.hdfs.server.federation.store.records.StateStoreVersion;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 定期发送心跳，即更新 Router基本信息 
// [包含 (MembershipStore + MountTableStore) 的最后一次更新时间，还包含 adminAddress]
public class RouterHeartbeatService extends PeriodicService {

    private static final Logger LOG = LoggerFactory.getLogger(RouterHeartbeatService.class);

    private final Router router;

    public RouterHeartbeatService(Router router) {
        super(RouterHeartbeatService.class.getSimpleName());
        this.router = router;
    }

    // 服务初始化，设置定期性执行的时间间隔
    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        long interval = conf.getTimeDuration(
                RBFConfigKeys.DFS_ROUTER_HEARTBEAT_STATE_INTERVAL_MS,
                RBFConfigKeys.DFS_ROUTER_HEARTBEAT_STATE_INTERVAL_MS_DEFAULT,
                TimeUnit.MILLISECONDS);
        this.setIntervalMs(interval);

        super.serviceInit(conf);
    }

    // 定期性调用 
    @Override
    public void periodicInvoke() {
        updateStateStore();
    }

    // 新起一个线程用来异步更新 Router的状态
    protected void updateStateAsync() {
        Thread thread = new Thread(() -> RouterHeartbeatService.this.updateStateStore(), "Router Heartbeat Async");
        thread.setDaemon(true);
        thread.start();
    }


    // 被 periodicInvoke()定期性调用, 和 updateStateAsync() 异步调用
    // 更新 Router基本信息  [包含 (MembershipStore + MountTableStore) 的最后一次更新时间，还包含 adminAddress]
    @VisibleForTesting
    synchronized void updateStateStore() {
        String routerId = router.getRouterId();
        if (isStoreAvailable()) {
            RouterStore routerStore = router.getRouterStateManager();
            try {
                RouterState record = RouterState.newInstance(routerId, router.getStartTime(), router.getRouterState());

                StateStoreVersion stateStoreVersion = StateStoreVersion.newInstance(
                        getStateStoreVersion(MembershipStore.class),
                        getStateStoreVersion(MountTableStore.class));

                // 从 Router里取的 routerState，包含 (MembershipStore + MountTableStore) 的最后一次更新时间，还包含 adminAddress
                // 再 request = RouterHeartbeatRequest.newInstance(routerState)
                record.setStateStoreVersion(stateStoreVersion);
                String hostPort = StateStoreUtils.getHostPortString(router.getAdminServerAddress());
                record.setAdminAddress(hostPort);

                RouterHeartbeatRequest request = RouterHeartbeatRequest.newInstance(record);
                // 核心点： 发送心跳, 即对 ZNode进行写数据
                // 即将缓存里的数据（(MembershipStore + MountTableStore)）和 adminAddress 更新到 ZNode
                RouterHeartbeatResponse response = routerStore.routerHeartbeat(request);
                if (!response.getStatus()) {
                    LOG.warn("Cannot heartbeat router {}", routerId);
                } else {
                    LOG.debug("Router heartbeat for router {}", routerId);
                }
            } catch (IOException e) {
                LOG.error("Cannot heartbeat router {}", routerId, e);
            }
        } else {
            LOG.warn("Cannot heartbeat router {}: State Store unavailable", routerId);
        }
    }

    // 获得 MembershipStore 和 MountTableStore的 lastUpdateTime
    private <R extends BaseRecord, S extends RecordStore<R>> long getStateStoreVersion(final Class<S> clazz) {
        long version = -1;
        try {
            StateStoreService stateStore = router.getStateStore();
            S recordStore = stateStore.getRegisteredRecordStore(clazz);
            if (recordStore != null) {
                if (recordStore instanceof CachedRecordStore) {
                    CachedRecordStore<R> cachedRecordStore = (CachedRecordStore<R>) recordStore;
                    List<R> records = cachedRecordStore.getCachedRecords();
                    for (BaseRecord record : records) {
                        if (record.getDateModified() > version) {
                            version = record.getDateModified();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot get version for {}", clazz, e);
        }
        return version;
    }

    // 检查 Router是否正常  (并不会去管安全模式)
    private boolean isStoreAvailable() {
        if (router.getRouterStateManager() == null) {
            return false;
        }
        if (router.getStateStore() == null) {
            return false;
        }
        return router.getStateStore().isDriverReady();
    }
}
