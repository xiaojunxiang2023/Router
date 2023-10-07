package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.router.PeriodicService;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

// 定期从 StateStore中更新缓存
public class StateStoreCacheUpdateService extends PeriodicService {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreCacheUpdateService.class);

    private final StateStoreService stateStore;


    public StateStoreCacheUpdateService(StateStoreService stateStore) {
        super(StateStoreCacheUpdateService.class.getSimpleName());
        this.stateStore = stateStore;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {

        this.setIntervalMs(conf.getTimeDuration(
                RBFConfigKeys.DFS_ROUTER_CACHE_TIME_TO_LIVE_MS,
                RBFConfigKeys.DFS_ROUTER_CACHE_TIME_TO_LIVE_MS_DEFAULT,
                TimeUnit.MILLISECONDS));

        super.serviceInit(conf);
    }

    // 定期方法
    @Override
    public void periodicInvoke() {
        LOG.debug("Updating State Store cache");
        stateStore.refreshCaches();
    }
}
