package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreService;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.apache.hadoop.util.Time.now;

// 定期执行，维护更新 Router的 safemode状态
public class RouterSafemodeService extends PeriodicService {

    private static final Logger LOG = LoggerFactory.getLogger(RouterSafemodeService.class);

    private final Router router;

    private volatile boolean safeMode;

    // 是否为手动设置的 safemode
    private volatile boolean isSafeModeSetManually;
    
    /*
        初始化配置
        赋值 startupTime
        进入安全模式并赋值 enterSafeModeTime
        所以 enterSafeModeTime略微大于 startupTime，微秒级别
     */
    
    private long startupTime;

    // 进入安全模式的时间
    private long enterSafeModeTime = now();
    
    // 刚启动的时候，多久才能自动退出 safemode
    private long startupInterval;
    
    // 最后一次缓存更新时间 距离当前时间超过这个值，就认为 stale了
    // 缓存来自于 Router里的 StateStoreService
    private long staleInterval;



    @Override
    protected void serviceInit(Configuration conf) throws Exception {

        this.setIntervalMs(conf.getTimeDuration(
                RBFConfigKeys.DFS_ROUTER_CACHE_TIME_TO_LIVE_MS,
                RBFConfigKeys.DFS_ROUTER_CACHE_TIME_TO_LIVE_MS_DEFAULT,
                TimeUnit.MILLISECONDS));

        this.startupInterval = conf.getTimeDuration(
                RBFConfigKeys.DFS_ROUTER_SAFEMODE_EXTENSION,
                RBFConfigKeys.DFS_ROUTER_SAFEMODE_EXTENSION_DEFAULT,
                TimeUnit.MILLISECONDS);
        LOG.info("Leave startup safe mode after {} ms", this.startupInterval);

        this.staleInterval = conf.getTimeDuration(
                RBFConfigKeys.DFS_ROUTER_SAFEMODE_EXPIRATION,
                RBFConfigKeys.DFS_ROUTER_SAFEMODE_EXPIRATION_DEFAULT,
                TimeUnit.MILLISECONDS);
        LOG.info("Enter safe mode after {} ms without reaching the State Store",
                this.staleInterval);

        this.startupTime = Time.now();

        // 进入 safemode
        enter();
        super.serviceInit(conf);
    }

    // 周期性检查：
    // 1、如果缓存 stale，则进入 safemode
    // 2、如果缓存已经恢复正常，且不是手动设置的进入 safemode，则自动退出 safemode
    @Override
    public void periodicInvoke() {
        long now = Time.now();
        long delta = now - startupTime;
        if (delta < startupInterval) {
            LOG.info("Delaying safemode exit for {} milliseconds...", this.startupInterval - delta);
            return;
        }
        StateStoreService stateStore = router.getStateStore();
        long cacheUpdateTime = stateStore.getCacheUpdateTime();
        boolean isCacheStale = (now - cacheUpdateTime) > this.staleInterval;

        // 缓存 stale，则进入 safemode
        if (isCacheStale) {
            if (!safeMode) {
                enter();
            }
        } else if (safeMode && !isSafeModeSetManually) {
            // Cache recently updated, leave safe mode
            leave();
        }
    }
    
    private void enter() {
        LOG.info("Entering safe mode");
        enterSafeModeTime = now();
        safeMode = true;
        // 更新 RouterServiceState
        router.updateRouterState(RouterServiceState.SAFEMODE);
    }

    private void leave() {
        // 记录离开 safemode的时间，打印日志 和 赋值给jmx指标
        long timeInSafemode = now() - enterSafeModeTime;
        LOG.info("Leaving safe mode after {} milliseconds", timeInSafemode);
        RouterMetrics routerMetrics = router.getRouterMetrics();
        if (routerMetrics == null) {
            LOG.error("The Router metrics are not enabled");
        } else {
            routerMetrics.setSafeModeTime(timeInSafemode);
        }
        safeMode = false;
        router.updateRouterState(RouterServiceState.RUNNING);
    }

    
    public RouterSafemodeService(Router router) {
        super(RouterSafemodeService.class.getSimpleName());
        this.router = router;
    }

    boolean isInSafeMode() {
        return this.safeMode;
    }

    void setManualSafeMode(boolean mode) {
        this.safeMode = mode;
        this.isSafeModeSetManually = mode;
    }

}