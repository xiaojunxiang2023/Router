package org.apache.hadoop.hdfs.server.federation.resolver.order;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.resolver.PathLocation;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.hdfs.server.federation.router.Router;
import org.apache.hadoop.hdfs.server.federation.router.RouterRpcServer;
import org.apache.hadoop.hdfs.server.federation.store.MembershipStore;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.hadoop.util.Time.monotonicNow;

// LocalResolver 和 AvailableSpaceResolver 的基类
public abstract class RouterResolver<K, V> implements OrderedResolver {

    private static final Logger LOG = LoggerFactory.getLogger(RouterResolver.class);


    private final Router router;

    // 一般为: ip -> info(可以是String)
    private Map<K, V> subclusterMapping = null;


    public static final String MIN_UPDATE_PERIOD_KEY =
            RBFConfigKeys.FEDERATION_ROUTER_PREFIX + "router-resolver.update-period";
    private static final long MIN_UPDATE_PERIOD_DEFAULT = TimeUnit.SECONDS
            .toMillis(10);
    private final long minUpdateTime;
    private long lastUpdated;

    public RouterResolver(final Configuration conf, final Router routerService) {
        this.minUpdateTime = conf.getTimeDuration(MIN_UPDATE_PERIOD_KEY,
                MIN_UPDATE_PERIOD_DEFAULT, TimeUnit.MILLISECONDS);
        this.router = routerService;
    }

    // 对外提供的方法，得到一个最优的 ns
    @Override
    public String getFirstNamespace(String path, PathLocation loc) {
        updateSubclusterMapping();
        return chooseFirstNamespace(path, loc);
    }

    // 定期更新挂载表缓存的线程
    private synchronized void updateSubclusterMapping() {
        if (subclusterMapping == null
                || (monotonicNow() - lastUpdated) > minUpdateTime) {
            Thread updater = new Thread(new Runnable() {
                @Override
                public void run() {
                    final MembershipStore membershipStore = getMembershipStore();
                    if (membershipStore == null) {
                        LOG.error("Cannot access the Membership store.");
                        return;
                    }

                    subclusterMapping = getSubclusterInfo(membershipStore);
                    lastUpdated = monotonicNow();
                }
            });
            updater.start();

            if (subclusterMapping == null) {
                try {
                    LOG.debug("Wait to get the mapping for the first time");
                    updater.join();
                } catch (InterruptedException e) {
                    LOG.error("Cannot wait for the updater to finish");
                }
            }
        }
    }

    protected abstract Map<K, V> getSubclusterInfo(MembershipStore membershipStore);

    protected abstract String chooseFirstNamespace(String path, PathLocation loc);


    protected RouterRpcServer getRpcServer() {
        if (this.router == null) {
            return null;
        }
        return router.getRpcServer();
    }

    protected MembershipStore getMembershipStore() {
        StateStoreService stateStore = router.getStateStore();
        if (stateStore == null) {
            return null;
        }
        return stateStore.getRegisteredRecordStore(MembershipStore.class);
    }

    protected Map<K, V> getSubclusterMapping() {
        return this.subclusterMapping;
    }
}
