package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.store.MountTableStore;
import org.apache.hadoop.hdfs.server.federation.store.RouterStore;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreUnavailableException;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreUtils;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.thirdparty.com.google.common.cache.*;
import org.apache.hadoop.thirdparty.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
   当 MountTableStore的挂载点发生变化时，便会调用这个类进行刷新缓存，
   会遍历对每个 Router进程都做刷新操作：   ? 什么逻辑,本机的话还复杂些, 且本质上都是调用 MountTableStore.loadCache(),哪里体现本地和远程了区别了?
       如果此 Router恰好位于本机，则使用 RouterAdminServer作为 MountTableManager传参给 MountTableRefresherThread
          这样的话，refresh()时就是 RouterAdminServer.refreshMountTableEntries()，会调用 MountTableResolver.loadCache(), 
          进一步调用 MountTableStore.loadCache()
       否则就是 MountTableStoreImpl.refreshMountTableEntries() 即也进一步调用 MountTableStore.loadCache()
 */

/*
   不是自己的定期服务，而是事件驱动(增、删、改 了挂载点的事件)
 */

/*
   为了提升性能，每个 Router都被分配了一个 MountTableRefresherThread线程，且被缓存起来
   内部有个 ScheduledExecutorService，但是是定期清理内部缓存：(host:port -> RouterClient) 
 */
public class MountTableRefresherService extends AbstractService {
    private static final String ROUTER_CONNECT_ERROR_MSG =
            "Router {} connection failed. Mount table cache will not refresh.";
    private static final Logger LOG = LoggerFactory.getLogger(MountTableRefresherService.class);

    // 本地的 router
    private final Router router;
    private String localAdminAddress;
    private long cacheUpdateTimeout;

    // 内部 RouterClient缓存：host:port -> RouterClient
    private LoadingCache<String, RouterClient> routerClientsCache;
    // 内部缓存的定期清理
    private ScheduledExecutorService clientCacheCleanerScheduler;

    public MountTableRefresherService(Router router) {
        super(MountTableRefresherService.class.getSimpleName());
        this.router = router;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        super.serviceInit(conf);
        MountTableStore mountTableStore = getMountTableStore();
        mountTableStore.setRefreshService(this);
        this.localAdminAddress = StateStoreUtils.getHostPortString(router.getAdminServerAddress());
        this.cacheUpdateTimeout = conf.getTimeDuration(
                RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE_TIMEOUT,
                RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE_TIMEOUT_DEFAULT,
                TimeUnit.MILLISECONDS);

        // 创建 RouterClient缓存
        long routerClientMaxLiveTime = conf.getTimeDuration(
                RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE_CLIENT_MAX_TIME,
                RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE_CLIENT_MAX_TIME_DEFAULT,
                TimeUnit.MILLISECONDS);
        routerClientsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(routerClientMaxLiveTime, TimeUnit.MILLISECONDS)
                .removalListener(getClientRemover()).build(getClientCreator());

///////////////////////////////// 定期清理缓存的逻辑   ////////////////////////////////////    
        initClientCacheCleaner(routerClientMaxLiveTime);
    }


    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                        .setNameFormat("MountTableRefresh_ClientsCacheCleaner")
                        .setDaemon(true).build());

        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    // 创建一个 Remove Listener
    private RemovalListener<String, RouterClient> getClientRemover() {
        return new RemovalListener<String, RouterClient>() {
            @Override
            public void onRemoval(RemovalNotification<String, RouterClient> notification) {
                closeRouterClient(notification.getValue());
            }
        };
    }

    @VisibleForTesting
    protected void closeRouterClient(RouterClient client) {
        client.close();
    }

///////////////////////////////// 定期清理缓存的逻辑   ////////////////////////////////////    


    // 创建一个 RouterClient，并被缓存包装，即放到缓存里
    private CacheLoader<String, RouterClient> getClientCreator() {
        return new CacheLoader<String, RouterClient>() {
            public RouterClient load(String adminAddress) throws IOException {
                InetSocketAddress routerSocket = NetUtils.createSocketAddr(adminAddress);
                Configuration config = getConfig();
                return createRouterClient(routerSocket, config);
            }
        };
    }

    @VisibleForTesting
    protected RouterClient createRouterClient(InetSocketAddress routerSocket,
                                              Configuration config) throws IOException {
        return SecurityUtil.doAsLoginUser(() -> {
            if (UserGroupInformation.isSecurityEnabled()) {
                UserGroupInformation.getLoginUser().checkTGTAndReloginFromKeytab();
            }
            return new RouterClient(routerSocket, config);
        });
    }


    @Override
    protected void serviceStart() throws Exception {
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
        clientCacheCleanerScheduler.shutdown();
        routerClientsCache.invalidateAll();
    }

    private MountTableStore getMountTableStore() throws IOException {
        MountTableStore mountTblStore = router.getStateStore().getRegisteredRecordStore(MountTableStore.class);
        if (mountTblStore == null) {
            throw new IOException("Mount table state store is not available.");
        }
        return mountTblStore;
    }

    // 核心方法，这次刷新的是 挂载表缓存 + 内部 RouterClient的缓存
    public void refresh() throws StateStoreUnavailableException {
        RouterStore routerStore = router.getRouterStateManager();

        try {
            // 刷新 router的缓存
            routerStore.loadCache(true);
        } catch (IOException e) {
            LOG.warn("RouterStore load cache failed,", e);
        }

        List<RouterState> cachedRecords = routerStore.getCachedRecords();
        List<MountTableRefresherThread> refreshThreads = new ArrayList<>();

        // 遍历 routerStore里取出来的所有 Router
        for (RouterState routerState : cachedRecords) {
            // adminAddress相当于 router的地址
            String adminAddress = routerState.getAdminAddress();
            if (adminAddress == null || adminAddress.length() == 0) {
                continue;
            }

            // 严重问题, 一直在 new MountTableRefresherThread, 线程刷新完一次缓存就被销毁了
            if (routerState.getStatus() != RouterServiceState.RUNNING) {
                // Router不处于 RUNNING了，则删除内部缓存中的该 RouterClient
                LOG.info("Router {} is not running. Mount table cache will not refresh.", routerState.getAddress());
                removeFromCache(adminAddress);
            } else if (isLocalAdmin(adminAddress)) {
                refreshThreads.add(getLocalRefresher(adminAddress));
            } else {
                try {
                    // 从 Router获取 mountTableManager，进行刷新挂载表
                    RouterClient client = routerClientsCache.get(adminAddress);
                    refreshThreads.add(new MountTableRefresherThread(client.getMountTableManager(), adminAddress));
                } catch (ExecutionException execExcep) {
                    LOG.warn(ROUTER_CONNECT_ERROR_MSG, adminAddress, execExcep);
                }
            }
        }
        if (!refreshThreads.isEmpty()) {
            invokeRefresh(refreshThreads);
        }
    }

    @VisibleForTesting
    protected MountTableRefresherThread getLocalRefresher(String adminAddress) {
        // RouterAdminServer是 MountTableManager的后代
        return new MountTableRefresherThread(router.getAdminServer(), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(List<MountTableRefresherThread> refreshThreads) {
        CountDownLatch countDownLatch = new CountDownLatch(refreshThreads.size());
        for (MountTableRefresherThread refThread : refreshThreads) {
            refThread.setCountDownLatch(countDownLatch);
            refThread.start();
        }
        try {
            boolean allReqCompleted = countDownLatch.await(cacheUpdateTimeout, TimeUnit.MILLISECONDS);
            if (!allReqCompleted) {
                LOG.warn("Not all router admins updated their cache");
            }
        } catch (InterruptedException e) {
            LOG.error("Mount table cache refresher was interrupted.", e);
        }
        logResult(refreshThreads);
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contentEquals(localAdminAddress);
    }

    private void logResult(List<MountTableRefresherThread> refreshThreads) {
        int successCount = 0;
        int failureCount = 0;
        for (MountTableRefresherThread mountTableRefreshThread : refreshThreads) {
            if (mountTableRefreshThread.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // 失败了就移除 RouterClient
                removeFromCache(mountTableRefreshThread.getAdminAddress());
            }
        }
        LOG.info("Mount table entries cache refresh successCount={},failureCount={}", successCount, failureCount);
    }
}
