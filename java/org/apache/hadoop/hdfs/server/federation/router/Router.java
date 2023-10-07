package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.HAUtil;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.server.common.TokenVerifier;
import org.apache.hadoop.hdfs.server.federation.metrics.NamenodeBeanMetrics;
import org.apache.hadoop.hdfs.server.federation.metrics.RBFMetrics;
import org.apache.hadoop.hdfs.server.federation.resolver.ActiveNamenodeResolver;
import org.apache.hadoop.hdfs.server.federation.resolver.FileSubclusterResolver;
import org.apache.hadoop.hdfs.server.federation.store.MountTableStore;
import org.apache.hadoop.hdfs.server.federation.store.RouterStore;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreService;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.metrics2.source.JvmMetrics;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.util.JvmPauseMonitor;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.hadoop.hdfs.server.federation.router.FederationUtil.newActiveNamenodeResolver;
import static org.apache.hadoop.hdfs.server.federation.router.FederationUtil.newFileSubclusterResolver;
import static org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys.*;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public class Router extends CompositeService implements TokenVerifier<DelegationTokenIdentifier> {

    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    private Configuration conf;
    private String routerId;
    private InetSocketAddress adminAddress;
    private InetSocketAddress rpcAddress;


    ///// Server：
    private RouterRpcServer rpcServer;
    private RouterHttpServer httpServer;
    private RouterAdminServer adminServer;

    ///// Service：
    private StateStoreService stateStore;
    private Collection<NamenodeHeartbeatService> namenodeHeartbeatServices;
    private RouterHeartbeatService routerHeartbeatService;
    private RouterSafemodeService safemodeService;
    private RouterQuotaUpdateService quotaUpdateService;
    private RouterMetricsService metrics;


    ///// 这些只是被赋值给 Service的属性
    private ActiveNamenodeResolver namenodeResolver;
    private FileSubclusterResolver subclusterResolver;
    private RouterQuotaManager quotaManager;
    private RouterStore routerStateManager;
    private JvmPauseMonitor pauseMonitor;


    private final long startTime = Time.now();
    private RouterServiceState state = RouterServiceState.UNINITIALIZED;

    public Router() {
        super(Router.class.getName());
    }

    /*
        添加一堆 Service：
            addService(this.stateStore);

            addService(this.rpcServer);
            addService(this.adminServer);
            addService(this.httpServer);
            
            addService(heartbeatService);
            addService(this.routerHeartbeatService);
            
            addService(refreshService);
            
            addService(this.metrics);
            addService(this.quotaUpdateService);
            addService(this.safemodeService);
     */
    @Override
    protected void serviceInit(Configuration configuration) throws Exception {
        this.conf = configuration;
        updateRouterState(RouterServiceState.INITIALIZING);

        UserGroupInformation.setConfiguration(conf);
        // 认证
        SecurityUtil.login(conf, DFS_ROUTER_KEYTAB_FILE_KEY,
                DFS_ROUTER_KERBEROS_PRINCIPAL_KEY, getHostName(conf));

        if (conf.getBoolean(RBFConfigKeys.DFS_ROUTER_STORE_ENABLE,
                RBFConfigKeys.DFS_ROUTER_STORE_ENABLE_DEFAULT)) {
            this.stateStore = new StateStoreService();
            addService(this.stateStore);
        }

        // Resolver to track active NNs
        this.namenodeResolver = newActiveNamenodeResolver(this.conf, this.stateStore);
        if (this.namenodeResolver == null) {
            throw new IOException("Cannot find namenode resolver.");
        }

        this.subclusterResolver = newFileSubclusterResolver(this.conf, this);
        if (this.subclusterResolver == null) {
            throw new IOException("Cannot find subcluster resolver");
        }

        if (conf.getBoolean(
                RBFConfigKeys.DFS_ROUTER_RPC_ENABLE,
                RBFConfigKeys.DFS_ROUTER_RPC_ENABLE_DEFAULT)) {
            this.rpcServer = createRpcServer();
            addService(this.rpcServer);
            this.setRpcServerAddress(rpcServer.getRpcAddress());
        }

        if (conf.getBoolean(
                RBFConfigKeys.DFS_ROUTER_ADMIN_ENABLE,
                RBFConfigKeys.DFS_ROUTER_ADMIN_ENABLE_DEFAULT)) {
            this.adminServer = createAdminServer();
            addService(this.adminServer);
        }

        if (conf.getBoolean(
                RBFConfigKeys.DFS_ROUTER_HTTP_ENABLE,
                RBFConfigKeys.DFS_ROUTER_HTTP_ENABLE_DEFAULT)) {
            this.httpServer = createHttpServer();
            addService(this.httpServer);
        }

        boolean isRouterHeartbeatEnabled = conf.getBoolean(RBFConfigKeys.DFS_ROUTER_HEARTBEAT_ENABLE,
                RBFConfigKeys.DFS_ROUTER_HEARTBEAT_ENABLE_DEFAULT);
        boolean isNamenodeHeartbeatEnable = conf.getBoolean(
                RBFConfigKeys.DFS_ROUTER_NAMENODE_HEARTBEAT_ENABLE, isRouterHeartbeatEnabled);
        if (isNamenodeHeartbeatEnable) {
            this.namenodeHeartbeatServices = createNamenodeHeartbeatServices();
            for (NamenodeHeartbeatService heartbeatService : this.namenodeHeartbeatServices) {
                addService(heartbeatService);
            }
            if (this.namenodeHeartbeatServices.isEmpty()) {
                LOG.error("Heartbeat is enabled but there are no namenodes to monitor");
            }
        }
        if (isRouterHeartbeatEnabled) {
            this.routerHeartbeatService = new RouterHeartbeatService(this);
            addService(this.routerHeartbeatService);
        }

        if (conf.getBoolean(RBFConfigKeys.DFS_ROUTER_METRICS_ENABLE,
                RBFConfigKeys.DFS_ROUTER_METRICS_ENABLE_DEFAULT)) {
            DefaultMetricsSystem.initialize("Router");

            this.metrics = new RouterMetricsService(this);
            addService(this.metrics);

            this.pauseMonitor = new JvmPauseMonitor();
            this.pauseMonitor.init(conf);
        }

        if (conf.getBoolean(RBFConfigKeys.DFS_ROUTER_QUOTA_ENABLE,
                RBFConfigKeys.DFS_ROUTER_QUOTA_ENABLED_DEFAULT)) {
            this.quotaManager = new RouterQuotaManager();
            this.quotaUpdateService = new RouterQuotaUpdateService(this);
            addService(this.quotaUpdateService);
        }

        if (conf.getBoolean(
                RBFConfigKeys.DFS_ROUTER_SAFEMODE_ENABLE,
                RBFConfigKeys.DFS_ROUTER_SAFEMODE_ENABLE_DEFAULT)) {
            this.safemodeService = new RouterSafemodeService(this);
            addService(this.safemodeService);
        }

        // 如果 RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE 为true，
        //   才注册事件驱动的挂载表刷新缓存Service：MountTableRefresherService
        // 默认值为 false
        if (conf.getBoolean(RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE, RBFConfigKeys.MOUNT_TABLE_CACHE_UPDATE_DEFAULT)) {
            String disabledDependentServices = getDisabledDependentServices();
            if (disabledDependentServices == null) {
                MountTableRefresherService refreshService = new MountTableRefresherService(this);
                addService(refreshService);
                LOG.info("Service {} is enabled.", MountTableRefresherService.class.getSimpleName());
            } else {
                LOG.warn("Service {} not enabled: dependent service(s) {} not enabled.",
                        MountTableRefresherService.class.getSimpleName(), disabledDependentServices);
            }
        }

        super.serviceInit(conf);

        // Set quota manager in mount store to update quota usage in mount table.
        if (stateStore != null) {
            // MountTableStore
            this.stateStore.getRegisteredRecordStore(MountTableStore.class).setQuotaManager(this.quotaManager);
        }
    }

    // 获得不可用的服务
    private String getDisabledDependentServices() {
        if (this.stateStore == null && this.adminServer == null) {
            return StateStoreService.class.getSimpleName() + "," + RouterAdminServer.class.getSimpleName();
        } else if (this.stateStore == null) {
            return StateStoreService.class.getSimpleName();
        } else if (this.adminServer == null) {
            return RouterAdminServer.class.getSimpleName();
        }
        return null;
    }

    // 根据 ...kerberos.principal.hostname获得 Router的 principal主机名
    private static String getHostName(Configuration config)
            throws UnknownHostException {
        String name = config.get(DFS_ROUTER_KERBEROS_PRINCIPAL_HOSTNAME_KEY);
        if (name == null) {
            name = InetAddress.getLocalHost().getHostName();
        }
        return name;
    }

    @Override
    protected void serviceStart() throws Exception {
        if (this.safemodeService == null) {
            // 如果没有开启 safemodeService，则直接将 RouterServiceState设为 Running
            updateRouterState(RouterServiceState.RUNNING);
        }

        if (this.pauseMonitor != null) {
            this.pauseMonitor.start();
            JvmMetrics jvmMetrics = this.metrics.getJvmMetrics();
            if (jvmMetrics != null) {
                jvmMetrics.setPauseMonitor(pauseMonitor);
            }
        }
        super.serviceStart();
    }

    public void shutDown() {
        // 新起一个线程，异步调用 serviceStop
        new Thread(Router.this::stop).start();
    }

    // create Server 或 Service
    protected RouterRpcServer createRpcServer() throws IOException {
        return new RouterRpcServer(this.conf, this, this.getNamenodeResolver(), this.getSubclusterResolver());
    }

    protected RouterAdminServer createAdminServer() throws IOException {
        return new RouterAdminServer(this.conf, this);
    }

    protected RouterHttpServer createHttpServer() {
        return new RouterHttpServer(this);
    }

    protected Collection<NamenodeHeartbeatService> createNamenodeHeartbeatServices() {
        Map<String, NamenodeHeartbeatService> ret = new HashMap<>();
        if (conf.getBoolean(RBFConfigKeys.DFS_ROUTER_MONITOR_LOCAL_NAMENODE,
                RBFConfigKeys.DFS_ROUTER_MONITOR_LOCAL_NAMENODE_DEFAULT)) {
            // 是否多加一个：创建本地的 namenode心跳Service，意思是从 conf的 dfs.namenode.rpc-address里去获取 namenode地址
            NamenodeHeartbeatService localHeartbeatService = createLocalNamenodeHeartbeatService();
            if (localHeartbeatService != null) {
                String nnDesc = localHeartbeatService.getNamenodeDesc();
                ret.put(nnDesc, localHeartbeatService);
            }
        }

        // 从 dfs.federation.router.monitor.namenode里去获取 namenode地址
        Collection<String> namenodes = this.conf.getTrimmedStringCollection(RBFConfigKeys.DFS_ROUTER_MONITOR_NAMENODE);
        for (String namenode : namenodes) {
            String[] namenodeSplit = namenode.split("\\.");
            String nsId = null;
            String nnId = null;
            if (namenodeSplit.length == 2) {
                nsId = namenodeSplit[0];
                nnId = namenodeSplit[1];
            } else if (namenodeSplit.length == 1) {
                nsId = namenode;
            } else {
                LOG.error("Wrong Namenode to monitor: {}", namenode);
            }
            if (nsId != null) {
                NamenodeHeartbeatService heartbeatService = createNamenodeHeartbeatService(nsId, nnId);
                if (heartbeatService != null) {
                    ret.put(heartbeatService.getNamenodeDesc(), heartbeatService);
                }
            }
        }

        return ret.values();
    }

    protected NamenodeHeartbeatService createLocalNamenodeHeartbeatService() {
        String nsId = DFSUtil.getNamenodeNameServiceId(conf);
        String nnId = null;
        if (HAUtil.isHAEnabled(conf, nsId)) {
            nnId = HAUtil.getNameNodeId(conf, nsId);
            if (nnId == null) {
                LOG.error("Cannot find namenode id for local {}", nsId);
            }
        }
        return createNamenodeHeartbeatService(nsId, nnId);
    }

    protected NamenodeHeartbeatService createNamenodeHeartbeatService(
            String nsId, String nnId) {
        LOG.info("Creating heartbeat service for Namenode {} in {}", nnId, nsId);
        return new NamenodeHeartbeatService(namenodeResolver, nsId, nnId);
    }

    // 验证token，tokenId和 password是否匹配
    @Override
    public void verifyToken(DelegationTokenIdentifier tokenId, byte[] password) throws IOException {
        getRpcServer().getRouterSecurityManager().verifyToken(tokenId, password);
    }

    // 更新下 RouterServiceState的状态
    public void updateRouterState(RouterServiceState newState) {
        this.state = newState;
        if (this.routerHeartbeatService != null) {
            this.routerHeartbeatService.updateStateAsync();
        }
    }


    /*
        get/set/is 方法
     */
    public RouterRpcServer getRpcServer() {
        return this.rpcServer;
    }

    protected void setRpcServerAddress(InetSocketAddress address) {
        this.rpcAddress = address;

        // Use the RPC address as our unique router Id
        if (this.rpcAddress != null) {
            try {
                String hostname = InetAddress.getLocalHost().getHostName();
                setRouterId(hostname + ":" + this.rpcAddress.getPort());
            } catch (UnknownHostException ex) {
                LOG.error("Cannot set unique router ID, address not resolvable {}",
                        this.rpcAddress);
            }
        }
    }

    public InetSocketAddress getRpcServerAddress() {
        return this.rpcAddress;
    }

    protected void setAdminServerAddress(InetSocketAddress address) {
        this.adminAddress = address;
    }

    public InetSocketAddress getAdminServerAddress() {
        return adminAddress;
    }

    public InetSocketAddress getHttpServerAddress() {
        if (httpServer != null) {
            return httpServer.getHttpAddress();
        }
        return null;
    }

    public RouterServiceState getRouterState() {
        return this.state;
    }

    public StateStoreService getStateStore() {
        return this.stateStore;
    }

    public RouterMetrics getRouterMetrics() {
        if (this.metrics != null) {
            return this.metrics.getRouterMetrics();
        }
        return null;
    }

    public RBFMetrics getMetrics() {
        if (this.metrics != null) {
            return this.metrics.getRBFMetrics();
        }
        return null;
    }

    public NamenodeBeanMetrics getNamenodeMetrics() throws IOException {
        if (this.metrics == null) {
            throw new IOException("Namenode metrics is not initialized");
        }
        return this.metrics.getNamenodeMetrics();
    }

    public FileSubclusterResolver getSubclusterResolver() {
        return this.subclusterResolver;
    }

    public ActiveNamenodeResolver getNamenodeResolver() {
        return this.namenodeResolver;
    }

    public RouterStore getRouterStateManager() {
        if (this.routerStateManager == null && this.stateStore != null) {
            this.routerStateManager = this.stateStore.getRegisteredRecordStore(
                    RouterStore.class);
        }
        return this.routerStateManager;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public boolean isRouterState(RouterServiceState routerState) {
        return routerState.equals(this.state);
    }

    public void setRouterId(String id) {
        this.routerId = id;
        if (this.stateStore != null) {
            this.stateStore.setIdentifier(this.routerId);
        }
        if (this.namenodeResolver != null) {
            this.namenodeResolver.setRouterId(this.routerId);
        }
    }

    public String getRouterId() {
        return this.routerId;
    }


    public boolean isQuotaEnabled() {
        return this.quotaManager != null;
    }

    public RouterQuotaManager getQuotaManager() {
        return this.quotaManager;
    }

    @VisibleForTesting
    RouterQuotaUpdateService getQuotaCacheUpdateService() {
        return this.quotaUpdateService;
    }

    @VisibleForTesting
    Collection<NamenodeHeartbeatService> getNamenodeHeartbeatServices() {
        return this.namenodeHeartbeatServices;
    }

    @VisibleForTesting
    RouterHeartbeatService getRouterHeartbeatService() {
        return this.routerHeartbeatService;
    }

    RouterSafemodeService getSafemodeService() {
        return this.safemodeService;
    }

    public RouterAdminServer getAdminServer() {
        return adminServer;
    }

}
