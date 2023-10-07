package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.router.PeriodicService;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 定期性地检查与 StateStore的连接的 Service，并可以打开连接
public class StateStoreConnectionMonitorService extends PeriodicService {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreConnectionMonitorService.class);

    private final StateStoreService stateStore;


    public StateStoreConnectionMonitorService(StateStoreService store) {
        super(StateStoreConnectionMonitorService.class.getSimpleName());
        this.stateStore = store;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        this.setIntervalMs(conf.getLong(
                RBFConfigKeys.FEDERATION_STORE_CONNECTION_TEST_MS,
                RBFConfigKeys.FEDERATION_STORE_CONNECTION_TEST_MS_DEFAULT));

        super.serviceInit(conf);
    }

    // 定期方法
    @Override
    public void periodicInvoke() {
        LOG.debug("Checking state store connection");
        if (!stateStore.isDriverReady()) {
            LOG.info("Attempting to open state store driver.");
            stateStore.loadDriver();
        }
    }

}
