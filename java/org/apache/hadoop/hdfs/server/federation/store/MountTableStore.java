package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.resolver.MountTableManager;
import org.apache.hadoop.hdfs.server.federation.router.MountTableRefresherService;
import org.apache.hadoop.hdfs.server.federation.router.RouterQuotaManager;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 对 MountTable(挂载表相关的信息) 的一些管理
@InterfaceAudience.Private
@InterfaceStability.Evolving
public abstract class MountTableStore extends CachedRecordStore<MountTable>
        implements MountTableManager {
    private static final Logger LOG =
            LoggerFactory.getLogger(MountTableStore.class);

    // 事件驱动刷新缓存 Service
    private MountTableRefresherService refreshService;
    // quotaManager
    private RouterQuotaManager quotaManager;

    public MountTableStore(StateStoreDriver driver) {
        super(MountTable.class, driver);
    }

    public void setRefreshService(MountTableRefresherService refreshService) {
        this.refreshService = refreshService;
    }

    public void setQuotaManager(RouterQuotaManager quotaManager) {
        this.quotaManager = quotaManager;
    }

    public RouterQuotaManager getQuotaManager() {
        return quotaManager;
    }

    // 更新所有 Routers的挂载表缓存 
    protected void updateCacheAllRouters() {
        if (refreshService != null) {
            try {
                refreshService.refresh();
            } catch (StateStoreUnavailableException e) {
                LOG.error("Cannot refresh mount table: state store not available", e);
            }
        }
    }

}
