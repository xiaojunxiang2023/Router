package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.StorageType;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus;
import org.apache.hadoop.hdfs.protocol.proto.RouterProtocolProtos.RouterAdminProtocolService;
import org.apache.hadoop.hdfs.protocolPB.RouterAdminProtocol;
import org.apache.hadoop.hdfs.protocolPB.RouterAdminProtocolPB;
import org.apache.hadoop.hdfs.protocolPB.RouterAdminProtocolServerSideTranslatorPB;
import org.apache.hadoop.hdfs.protocolPB.RouterPolicyProvider;
import org.apache.hadoop.hdfs.server.federation.resolver.ActiveNamenodeResolver;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamespaceInfo;
import org.apache.hadoop.hdfs.server.federation.resolver.MountTableResolver;
import org.apache.hadoop.hdfs.server.federation.resolver.RemoteLocation;
import org.apache.hadoop.hdfs.server.federation.store.DisabledNameserviceStore;
import org.apache.hadoop.hdfs.server.federation.store.MountTableStore;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreCache;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.ipc.*;
import org.apache.hadoop.ipc.RPC.Server;
import org.apache.hadoop.ipc.proto.GenericRefreshProtocolProtos;
import org.apache.hadoop.ipc.proto.RefreshCallQueueProtocolProtos;
import org.apache.hadoop.ipc.protocolPB.GenericRefreshProtocolPB;
import org.apache.hadoop.ipc.protocolPB.GenericRefreshProtocolServerSideTranslatorPB;
import org.apache.hadoop.ipc.protocolPB.RefreshCallQueueProtocolPB;
import org.apache.hadoop.ipc.protocolPB.RefreshCallQueueProtocolServerSideTranslatorPB;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authorize.ProxyUsers;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.thirdparty.com.google.common.base.Preconditions;
import org.apache.hadoop.thirdparty.protobuf.BlockingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHORIZATION;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_PERMISSIONS_ENABLED_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_PERMISSIONS_ENABLED_KEY;

public class RouterAdminServer extends AbstractService
        implements RouterAdminProtocol, RefreshCallQueueProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(RouterAdminServer.class);

    private Configuration conf;

    private final Router router;

    private MountTableStore mountTableStore;
    private DisabledNameserviceStore disabledStore;

    // RPCServer 服务端代理
    private final Server adminServer;
    private final InetSocketAddress adminAddress;

    private static String routerOwner;
    private static String superGroup;
    private static boolean isPermissionEnabled;

    private final boolean iStateStoreCache;

    // 初始化配置, 创建 RPCServer, 添加协议映射
    public RouterAdminServer(Configuration conf, Router router) throws IOException {
        super(RouterAdminServer.class.getName());

        this.conf = conf;
        this.router = router;
        int handlerCount = this.conf.getInt(
                RBFConfigKeys.DFS_ROUTER_ADMIN_HANDLER_COUNT_KEY,
                RBFConfigKeys.DFS_ROUTER_ADMIN_HANDLER_COUNT_DEFAULT);

        RPC.setProtocolEngine(this.conf, RouterAdminProtocolPB.class,
                ProtobufRpcEngine2.class);
        RouterAdminProtocolServerSideTranslatorPB routerAdminProtocolTranslator =
                new RouterAdminProtocolServerSideTranslatorPB(this);
        BlockingService clientNNPbService = RouterAdminProtocolService.
                newReflectiveBlockingService(routerAdminProtocolTranslator);
        InetSocketAddress confRpcAddress = conf.getSocketAddr(
                RBFConfigKeys.DFS_ROUTER_ADMIN_BIND_HOST_KEY,
                RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_KEY,
                RBFConfigKeys.DFS_ROUTER_ADMIN_ADDRESS_DEFAULT,
                RBFConfigKeys.DFS_ROUTER_ADMIN_PORT_DEFAULT);
        String bindHost = conf.get(
                RBFConfigKeys.DFS_ROUTER_ADMIN_BIND_HOST_KEY,
                confRpcAddress.getHostName());
        LOG.info("Admin server binding to {}:{}",
                bindHost, confRpcAddress.getPort());

        // 初始化权限设置
        initializePermissionSettings(this.conf);

        this.adminServer = new RPC.Builder(this.conf)
                .setProtocol(RouterAdminProtocolPB.class)
                .setInstance(clientNNPbService)
                .setBindAddress(bindHost)
                .setPort(confRpcAddress.getPort())
                .setNumHandlers(handlerCount)
                .setVerbose(false)
                .build();

        // hadoop.security.authorization为true，则刷新下 协议客户端ACL
        if (conf.getBoolean(HADOOP_SECURITY_AUTHORIZATION, false)) {
            this.adminServer.refreshServiceAcl(conf, new RouterPolicyProvider());
        }

        InetSocketAddress listenAddress = this.adminServer.getListenerAddress();
        this.adminAddress = new InetSocketAddress(confRpcAddress.getHostName(), listenAddress.getPort());
        router.setAdminServerAddress(this.adminAddress);
        iStateStoreCache = router.getSubclusterResolver() instanceof StateStoreCache;


        // 添加 GenericRefreshProtocolPB(刷新 Router参数)、RefreshCallQueueProtocolPB(刷新 CallQueue)协议的映射
        GenericRefreshProtocolServerSideTranslatorPB genericRefreshXlator = new GenericRefreshProtocolServerSideTranslatorPB(this);
        BlockingService genericRefreshService = GenericRefreshProtocolProtos
                .GenericRefreshProtocolService.newReflectiveBlockingService(genericRefreshXlator);

        RefreshCallQueueProtocolServerSideTranslatorPB refreshCallQueueXlator =
                new RefreshCallQueueProtocolServerSideTranslatorPB(this);
        BlockingService refreshCallQueueService = RefreshCallQueueProtocolProtos
                .RefreshCallQueueProtocolService.newReflectiveBlockingService(refreshCallQueueXlator);

        DFSUtil.addPBProtocol(conf, GenericRefreshProtocolPB.class, genericRefreshService, adminServer);
        DFSUtil.addPBProtocol(conf, RefreshCallQueueProtocolPB.class, refreshCallQueueService, adminServer);
    }

    private static void initializePermissionSettings(Configuration routerConf)
            throws IOException {
        routerOwner = UserGroupInformation.getCurrentUser().getShortUserName();
        superGroup = routerConf.get(
                DFSConfigKeys.DFS_PERMISSIONS_SUPERUSERGROUP_KEY,
                DFSConfigKeys.DFS_PERMISSIONS_SUPERUSERGROUP_DEFAULT);
        isPermissionEnabled = routerConf.getBoolean(DFS_PERMISSIONS_ENABLED_KEY,
                DFS_PERMISSIONS_ENABLED_DEFAULT);
    }

    @Override
    protected void serviceInit(Configuration configuration) throws Exception {
        this.conf = configuration;
        super.serviceInit(conf);
    }

    @Override
    protected void serviceStart() throws Exception {
        this.adminServer.start();
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        if (this.adminServer != null) {
            this.adminServer.stop();
        }
        super.serviceStop();
    }

    /*
        挂载表相关, 靠 MountTableStore增删改查刷 挂载点
     */
    @Override
    public GetMountTableEntriesResponse getMountTableEntries(GetMountTableEntriesRequest request)
            throws IOException {
        return getMountTableStore().getMountTableEntries(request);
    }

    @Override
    public AddMountTableEntryResponse addMountTableEntry(AddMountTableEntryRequest request)
            throws IOException {
        return getMountTableStore().addMountTableEntry(request);
    }

    // 删除挂载点前，还 刷新挂载表缓存 和 重新初始化Quota [nsQuota 和 spQuata都重置为初始值，storageType重置为null]
    @Override
    public RemoveMountTableEntryResponse removeMountTableEntry(RemoveMountTableEntryRequest request)
            throws IOException {
        try {
            synchronizeQuota(request.getSrcPath(), HdfsConstants.QUOTA_RESET,
                    HdfsConstants.QUOTA_RESET, null);
        } catch (Exception e) {
            LOG.warn("Unable to clear quota at the destinations for {}: {}",
                    request.getSrcPath(), e.getMessage());
        }
        return getMountTableStore().removeMountTableEntry(request);
    }


    // 刷新挂载表缓存 和 更新Quota
    private void synchronizeQuota(String path, long nsQuota, long ssQuota,
                                  StorageType type) throws IOException {
        if (isQuotaSyncRequired(nsQuota, ssQuota)) {
            if (iStateStoreCache) {
                ((StateStoreCache) this.router.getSubclusterResolver()).loadCache(true);
            }
            Quota routerQuota = this.router.getRpcServer().getQuotaModule();
            routerQuota.setQuota(path, nsQuota, ssQuota, type, false);
        }
    }

    private boolean isQuotaSyncRequired(long nsQuota, long ssQuota) {
        if (router.isQuotaEnabled()) {
            return nsQuota != HdfsConstants.QUOTA_DONT_SET || ssQuota != HdfsConstants.QUOTA_DONT_SET;
        }
        return false;
    }

    // 更新挂载点后，还判断这个挂载点的 Destinations是否有更新，有的话就更新 Quota
    @Override
    public UpdateMountTableEntryResponse updateMountTableEntry(UpdateMountTableEntryRequest request)
            throws IOException {
        MountTable updateEntry = request.getEntry();
        MountTable oldEntry = null;
        if (this.router.getSubclusterResolver() instanceof MountTableResolver) {
            MountTableResolver mResolver =
                    (MountTableResolver) this.router.getSubclusterResolver();
            oldEntry = mResolver.getMountPoint(updateEntry.getSourcePath());
        }
        UpdateMountTableEntryResponse response = getMountTableStore().updateMountTableEntry(request);
        try {
            if (updateEntry != null && router.isQuotaEnabled()) {
                // 更新 Quota值（属于变更节点的情况，但是又没考虑 storageType）
                if (isQuotaUpdated(request, oldEntry)) {
                    synchronizeQuota(updateEntry.getSourcePath(),
                            updateEntry.getQuota().getQuota(),
                            updateEntry.getQuota().getSpaceQuota(), null);
                }

                // 考虑新增节点情况， 
                // 同时考虑变更节点的 storageType
                RouterQuotaUsage newQuota = request.getEntry().getQuota();
                boolean locationsChanged = oldEntry == null ||
                        !oldEntry.getDestinations().equals(updateEntry.getDestinations());
                for (StorageType t : StorageType.values()) {
                    if (locationsChanged || oldEntry.getQuota().getTypeQuota(t)
                            != newQuota.getTypeQuota(t)) {
                        synchronizeQuota(updateEntry.getSourcePath(),
                                HdfsConstants.QUOTA_DONT_SET, newQuota.getTypeQuota(t), t);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to reset quota at the destinations for {}: {}", request.getEntry(), e.getMessage());
        }
        return response;
    }

    private boolean isQuotaUpdated(UpdateMountTableEntryRequest request,
                                   MountTable oldEntry) throws IOException {
        if (oldEntry != null) {
            MountTable updateEntry = request.getEntry();
            if (!oldEntry.getDestinations().equals(updateEntry.getDestinations())) {
                return true;
            }

            // 旧的 quota
            RouterQuotaUsage preQuota = oldEntry.getQuota();
            long nsQuota = preQuota.getQuota();
            long ssQuota = preQuota.getSpaceQuota();

            // 新的 quota
            RouterQuotaUsage mountQuota = updateEntry.getQuota();
            return nsQuota != mountQuota.getQuota() || ssQuota != mountQuota.getSpaceQuota();
        } else {
            return true;
        }
    }

    @Override
    public RefreshMountTableEntriesResponse refreshMountTableEntries(RefreshMountTableEntriesRequest request)
            throws IOException {
        if (iStateStoreCache) {  // 只是判断是不是缓存类型，一般都是
            boolean result = ((StateStoreCache) this.router.getSubclusterResolver()).loadCache(true);
            RefreshMountTableEntriesResponse response = RefreshMountTableEntriesResponse.newInstance();
            response.setResult(result);
            return response;
        } else {
            return getMountTableStore().refreshMountTableEntries(request);
        }
    }


    /*
        safemode相关，靠 RouterSafemodeService来实现
     */
    @Override
    public EnterSafeModeResponse enterSafeMode(EnterSafeModeRequest request) throws IOException {
        // 检查是不是超级用户权限
        checkSuperuserPrivilege();
        boolean success = false;
        RouterSafemodeService safeModeService = this.router.getSafemodeService();
        if (safeModeService != null) {
            this.router.updateRouterState(RouterServiceState.SAFEMODE);
            safeModeService.setManualSafeMode(true);
            success = verifySafeMode(true);
            if (success) {
                LOG.info("STATE* Safe mode is ON.\n" + "It was turned on manually. "
                        + "Use \"hdfs dfsrouteradmin -safemode leave\" to turn"
                        + " safe mode off.");
            } else {
                LOG.error("Unable to enter safemode.");
            }
        }
        return EnterSafeModeResponse.newInstance(success);
    }

    void checkSuperuserPrivilege() throws AccessControlException {
        RouterPermissionChecker pc = RouterAdminServer.getPermissionChecker();
        if (pc != null) {
            pc.checkSuperuserPrivilege();
        }
    }

    // 是否为合法的 safemode
    private boolean verifySafeMode(boolean isInSafeMode) {
        Preconditions.checkNotNull(this.router.getSafemodeService());
        boolean serverInSafeMode = this.router.getSafemodeService().isInSafeMode();
        RouterServiceState currentState = this.router.getRouterState();
        return (isInSafeMode && currentState == RouterServiceState.SAFEMODE && serverInSafeMode)
                || (!isInSafeMode && currentState != RouterServiceState.SAFEMODE && !serverInSafeMode);
    }

    @Override
    public LeaveSafeModeResponse leaveSafeMode(LeaveSafeModeRequest request) throws IOException {
        checkSuperuserPrivilege();
        boolean success = false;
        RouterSafemodeService safeModeService = this.router.getSafemodeService();
        if (safeModeService != null) {
            this.router.updateRouterState(RouterServiceState.RUNNING);
            safeModeService.setManualSafeMode(false);
            success = verifySafeMode(false);
            if (success) {
                LOG.info("STATE* Safe mode is OFF.\n" + "It was turned off manually.");
            } else {
                LOG.error("Unable to leave safemode.");
            }
        }
        return LeaveSafeModeResponse.newInstance(success);
    }

    @Override
    public GetSafeModeResponse getSafeMode(GetSafeModeRequest request) throws IOException {
        boolean isInSafeMode = false;
        RouterSafemodeService safeModeService = this.router.getSafemodeService();
        if (safeModeService != null) {
            isInSafeMode = safeModeService.isInSafeMode();
            LOG.info("Safemode status retrieved successfully.");
        }
        return GetSafeModeResponse.newInstance(isInSafeMode);
    }


    /*
        getDestination相关
     */
    @Override
    public GetDestinationResponse getDestination(GetDestinationRequest request) throws IOException {
        final String src = request.getSrcPath();
        final List<String> nsIds = new ArrayList<>();
        RouterRpcServer rpcServer = this.router.getRpcServer();

        // 通过 getLocationsForPath底层调用 subclusterResolver就已经得到返回值了，
        // 但是还要多一步 getFileInfo的校验
        List<RemoteLocation> locations = rpcServer.getLocationsForPath(src, false);
        RouterRpcClient rpcClient = rpcServer.getRPCClient();
        
        RemoteMethod method = new RemoteMethod("getFileInfo",
                new Class<?>[]{String.class}, new RemoteParam());
        try {
            Map<RemoteLocation, HdfsFileStatus> responses = rpcClient.invokeConcurrent(
                    locations, method, false, false, HdfsFileStatus.class);
            for (RemoteLocation location : locations) {
                if (responses.get(location) != null) {
                    nsIds.add(location.getNameserviceId());
                }
            }
        } catch (IOException ioe) {
            LOG.error("Cannot get location for {}: {}", src, ioe.getMessage());
        }
        if (nsIds.isEmpty() && !locations.isEmpty()) {
            String nsId = locations.get(0).getNameserviceId();
            nsIds.add(nsId);
        }
        return GetDestinationResponse.newInstance(nsIds);
    }


    /*
       禁用 ns相关, 靠的是 DisabledNameserviceStore
     */
    @Override
    public DisableNameserviceResponse disableNameservice(DisableNameserviceRequest request)
            throws IOException {
        checkSuperuserPrivilege();

        String nsId = request.getNameServiceId();
        boolean success = false;
        if (namespaceExists(nsId)) {
            success = getDisabledNameserviceStore().disableNameservice(nsId);
            if (success) {
                LOG.info("Nameservice {} disabled successfully.", nsId);
            } else {
                LOG.error("Unable to disable Nameservice {}", nsId);
            }
        } else {
            LOG.error("Cannot disable {}, it does not exists", nsId);
        }
        return DisableNameserviceResponse.newInstance(success);
    }

    private boolean namespaceExists(final String nsId) throws IOException {
        boolean found = false;
        ActiveNamenodeResolver resolver = router.getNamenodeResolver();
        Set<FederationNamespaceInfo> nss = resolver.getNamespaces();
        for (FederationNamespaceInfo ns : nss) {
            if (nsId.equals(ns.getNameserviceId())) {
                found = true;
                break;
            }
        }
        return found;
    }

    @Override
    public EnableNameserviceResponse enableNameservice(EnableNameserviceRequest request)
            throws IOException {
        checkSuperuserPrivilege();

        String nsId = request.getNameServiceId();
        DisabledNameserviceStore store = getDisabledNameserviceStore();
        Set<String> disabled = store.getDisabledNameservices();
        boolean success = false;
        if (disabled.contains(nsId)) {
            success = store.enableNameservice(nsId);
            if (success) {
                LOG.info("Nameservice {} enabled successfully.", nsId);
            } else {
                LOG.error("Unable to enable Nameservice {}", nsId);
            }
        } else {
            LOG.error("Cannot enable {}, it was not disabled", nsId);
        }
        return EnableNameserviceResponse.newInstance(success);
    }

    @Override
    public GetDisabledNameservicesResponse getDisabledNameservices(
            GetDisabledNameservicesRequest request) throws IOException {
        Set<String> nsIds = getDisabledNameserviceStore().getDisabledNameservices();
        return GetDisabledNameservicesResponse.newInstance(nsIds);
    }


    /*
        -refreshRouterArgs的实现，刷新 Router参数 
     */
    @Override // GenericRefreshProtocol
    public Collection<RefreshResponse> refresh(String identifier, String[] args) {
        //  ? 底层怎么实现的
        return RefreshRegistry.defaultRegistry().dispatch(identifier, args);
    }

    /*
        刷新超级用户的配置
     */
    @Override // RouterGenericManager
    public boolean refreshSuperUserGroupsConfiguration() {
        ProxyUsers.refreshSuperUserGroupsConfiguration();
        return true;
    }

    /*
        刷新 CallQueue，靠 RPCServer来实现
     */
    @Override // RefreshCallQueueProtocol
    public void refreshCallQueue() {
        LOG.info("Refreshing call queue.");
        Configuration configuration = new Configuration();
        router.getRpcServer().getServer().refreshCallQueue(configuration);
    }
    
    
    
    /*
        get/set 方法
     */

    Server getAdminServer() {
        return this.adminServer;
    }

    private MountTableStore getMountTableStore() throws IOException {
        if (this.mountTableStore == null) {
            this.mountTableStore = router.getStateStore()
                    .getRegisteredRecordStore(MountTableStore.class);
            if (this.mountTableStore == null) {
                throw new IOException("Mount table state store is not available.");
            }
        }
        return this.mountTableStore;
    }


    private DisabledNameserviceStore getDisabledNameserviceStore() throws IOException {
        if (this.disabledStore == null) {
            this.disabledStore = router.getStateStore()
                    .getRegisteredRecordStore(DisabledNameserviceStore.class);
            if (this.disabledStore == null) {
                throw new IOException("Disabled Nameservice state store is not available.");
            }
        }
        return this.disabledStore;
    }


    public InetSocketAddress getRpcAddress() {
        return this.adminAddress;
    }

    public static RouterPermissionChecker getPermissionChecker()
            throws AccessControlException {
        if (!isPermissionEnabled) {
            return null;
        }

        try {
            return new RouterPermissionChecker(routerOwner, superGroup,
                    NameNode.getRemoteUser());
        } catch (IOException e) {
            throw new AccessControlException(e);
        }
    }

    public static String getSuperUser() {
        return routerOwner;
    }

    public static String getSuperGroup() {
        return superGroup;
    }

}
