package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.crypto.CryptoProtocolVersion;
import org.apache.hadoop.fs.BatchedRemoteIterator.BatchedEntries;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclStatus;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.ha.HAServiceProtocol;
import org.apache.hadoop.hdfs.AddBlockFlag;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.inotify.EventBatchList;
import org.apache.hadoop.hdfs.protocol.*;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.ReencryptAction;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.RollingUpgradeAction;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.protocol.OpenFilesIterator.OpenFilesType;
import org.apache.hadoop.hdfs.protocol.proto.ClientNamenodeProtocolProtos.ClientNamenodeProtocol;
import org.apache.hadoop.hdfs.protocol.proto.NamenodeProtocolProtos.NamenodeProtocolService;
import org.apache.hadoop.hdfs.protocolPB.*;
import org.apache.hadoop.hdfs.security.token.block.DataEncryptionKey;
import org.apache.hadoop.hdfs.security.token.block.ExportedBlockKeys;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.server.federation.metrics.FederationRPCMetrics;
import org.apache.hadoop.hdfs.server.federation.resolver.*;
import org.apache.hadoop.hdfs.server.federation.router.security.RouterSecurityManager;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreUnavailableException;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.apache.hadoop.hdfs.server.namenode.CheckpointSignature;
import org.apache.hadoop.hdfs.server.namenode.LeaseExpiredException;
import org.apache.hadoop.hdfs.server.namenode.NameNode.OperationCategory;
import org.apache.hadoop.hdfs.server.namenode.NotReplicatedYetException;
import org.apache.hadoop.hdfs.server.namenode.SafeModeException;
import org.apache.hadoop.hdfs.server.protocol.*;
import org.apache.hadoop.io.EnumSetWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.*;
import org.apache.hadoop.ipc.RPC.Server;
import org.apache.hadoop.net.NodeBase;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.RefreshUserMappingsProtocol;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.proto.RefreshUserMappingsProtocolProtos;
import org.apache.hadoop.security.protocolPB.RefreshUserMappingsProtocolPB;
import org.apache.hadoop.security.protocolPB.RefreshUserMappingsProtocolServerSideTranslatorPB;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.thirdparty.protobuf.BlockingService;
import org.apache.hadoop.tools.GetUserMappingsProtocol;
import org.apache.hadoop.tools.proto.GetUserMappingsProtocolProtos;
import org.apache.hadoop.tools.protocolPB.GetUserMappingsProtocolPB;
import org.apache.hadoop.tools.protocolPB.GetUserMappingsProtocolServerSideTranslatorPB;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHORIZATION;
import static org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys.*;

// 接收 DFSClient发送来的请求，再转派给 RouterRpcClient
public class RouterRpcServer extends AbstractService implements ClientProtocol,
        NamenodeProtocol, RefreshUserMappingsProtocol, GetUserMappingsProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(RouterRpcServer.class);
    private Configuration conf;

    private final Router router;
    private final Server rpcServer;
    private final InetSocketAddress rpcAddress;

    private final RouterRpcClient rpcClient;

    private final ActiveNamenodeResolver namenodeResolver;
    private final FileSubclusterResolver subclusterResolver;


    /*
        客户端
     */
    private final RouterClientProtocol clientProto;
    // secondaryNN的协议, 用来获取 NameNode信息
    private final RouterNamenodeProtocol nnProto;
    private final RouterUserProtocol routerProto;
    // 使用 Quota向 NameNode发起 RPC调用
    private final Quota quotaCall;

    private RouterSecurityManager securityManager;

    // 保存 superUser的凭证
    private static final ThreadLocal<UserGroupInformation> CUR_USER = new ThreadLocal<>();

    // 操作，如 READ、WRITE、CHECKPOINT
    private final ThreadLocal<OperationCategory> opCategory = new ThreadLocal<>();

    private final RouterRpcMonitor rpcMonitor;

    // 普通地 给属性赋值
    public RouterRpcServer(Configuration configuration, Router router, ActiveNamenodeResolver nnResolver,
                           FileSubclusterResolver fileResolver) throws IOException {
        super(RouterRpcServer.class.getName());

        this.conf = configuration;
        this.router = router;
        this.namenodeResolver = nnResolver;
        this.subclusterResolver = fileResolver;

        int handlerCount = this.conf.getInt(DFS_ROUTER_HANDLER_COUNT_KEY,
                DFS_ROUTER_HANDLER_COUNT_DEFAULT);

        int readerCount = this.conf.getInt(DFS_ROUTER_READER_COUNT_KEY,
                DFS_ROUTER_READER_COUNT_DEFAULT);

        int handlerQueueSize = this.conf.getInt(DFS_ROUTER_HANDLER_QUEUE_SIZE_KEY,
                DFS_ROUTER_HANDLER_QUEUE_SIZE_DEFAULT);

        int readerQueueSize = this.conf.getInt(DFS_ROUTER_READER_QUEUE_SIZE_KEY,
                DFS_ROUTER_READER_QUEUE_SIZE_DEFAULT);
        this.conf.setInt(
                CommonConfigurationKeys.IPC_SERVER_RPC_READ_CONNECTION_QUEUE_SIZE_KEY,
                readerQueueSize);

        RPC.setProtocolEngine(this.conf, ClientNamenodeProtocolPB.class,
                ProtobufRpcEngine2.class);

        ClientNamenodeProtocolServerSideTranslatorPB
                clientProtocolServerTranslator =
                new ClientNamenodeProtocolServerSideTranslatorPB(this);
        BlockingService clientNNPbService = ClientNamenodeProtocol
                .newReflectiveBlockingService(clientProtocolServerTranslator);

        NamenodeProtocolServerSideTranslatorPB namenodeProtocolXlator =
                new NamenodeProtocolServerSideTranslatorPB(this);
        BlockingService nnPbService = NamenodeProtocolService
                .newReflectiveBlockingService(namenodeProtocolXlator);

        RefreshUserMappingsProtocolServerSideTranslatorPB refreshUserMappingXlator =
                new RefreshUserMappingsProtocolServerSideTranslatorPB(this);
        BlockingService refreshUserMappingService =
                RefreshUserMappingsProtocolProtos.RefreshUserMappingsProtocolService.
                        newReflectiveBlockingService(refreshUserMappingXlator);

        GetUserMappingsProtocolServerSideTranslatorPB getUserMappingXlator =
                new GetUserMappingsProtocolServerSideTranslatorPB(this);
        BlockingService getUserMappingService =
                GetUserMappingsProtocolProtos.GetUserMappingsProtocolService.
                        newReflectiveBlockingService(getUserMappingXlator);

        InetSocketAddress confRpcAddress = conf.getSocketAddr(
                RBFConfigKeys.DFS_ROUTER_RPC_BIND_HOST_KEY,
                RBFConfigKeys.DFS_ROUTER_RPC_ADDRESS_KEY,
                RBFConfigKeys.DFS_ROUTER_RPC_ADDRESS_DEFAULT,
                RBFConfigKeys.DFS_ROUTER_RPC_PORT_DEFAULT);
        LOG.info("RPC server binding to {} with {} handlers for Router {}",
                confRpcAddress, handlerCount, this.router.getRouterId());

        // Create security manager
        this.securityManager = new RouterSecurityManager(this.conf);

        this.rpcServer = new RPC.Builder(this.conf)
                .setProtocol(ClientNamenodeProtocolPB.class)
                .setInstance(clientNNPbService)
                .setBindAddress(confRpcAddress.getHostName())
                .setPort(confRpcAddress.getPort())
                .setNumHandlers(handlerCount)
                .setnumReaders(readerCount)
                .setQueueSizePerHandler(handlerQueueSize)
                .setVerbose(false)
                .setSecretManager(this.securityManager.getSecretManager())
                .build();

        // Add all the RPC protocols that the Router implements
        DFSUtil.addPBProtocol(
                conf, NamenodeProtocolPB.class, nnPbService, this.rpcServer);
        DFSUtil.addPBProtocol(conf, RefreshUserMappingsProtocolPB.class,
                refreshUserMappingService, this.rpcServer);
        DFSUtil.addPBProtocol(conf, GetUserMappingsProtocolPB.class,
                getUserMappingService, this.rpcServer);

        // 是否开启了安全认证，如果开启了那就刷新下 协议客户端的 ACL
        boolean serviceAuthEnabled = conf.getBoolean(
                HADOOP_SECURITY_AUTHORIZATION, false);
        if (serviceAuthEnabled) {
            rpcServer.refreshServiceAcl(conf, new RouterPolicyProvider());
        }

        // 不要记录这些 异常的堆栈
        this.rpcServer.addTerseExceptions(
                RemoteException.class,
                SafeModeException.class,
                FileNotFoundException.class,
                FileAlreadyExistsException.class,
                AccessControlException.class,
                LeaseExpiredException.class,
                NotReplicatedYetException.class,
                IOException.class,
                ConnectException.class,
                RetriableException.class);
        // 不记录这个异常
        this.rpcServer.addSuppressedLoggingExceptions(StandbyException.class);

        InetSocketAddress listenAddress = this.rpcServer.getListenerAddress();
        this.rpcAddress = new InetSocketAddress(confRpcAddress.getHostName(), listenAddress.getPort());

        if (conf.getBoolean(RBFConfigKeys.DFS_ROUTER_METRICS_ENABLE,
                RBFConfigKeys.DFS_ROUTER_METRICS_ENABLE_DEFAULT)) {
            Class<? extends RouterRpcMonitor> rpcMonitorClass = this.conf.getClass(
                    RBFConfigKeys.DFS_ROUTER_METRICS_CLASS,
                    RBFConfigKeys.DFS_ROUTER_METRICS_CLASS_DEFAULT,
                    RouterRpcMonitor.class);
            this.rpcMonitor = ReflectionUtils.newInstance(rpcMonitorClass, conf);
        } else {
            this.rpcMonitor = null;
        }

        this.rpcClient = new RouterRpcClient(this.conf, this.router,
                this.namenodeResolver, this.rpcMonitor);

        this.quotaCall = new Quota(this.router, this);
        this.nnProto = new RouterNamenodeProtocol(this);
        this.clientProto = new RouterClientProtocol(conf, this);
        this.routerProto = new RouterUserProtocol(this);
    }

    @Override
    protected void serviceInit(Configuration configuration) throws Exception {
        this.conf = configuration;
        if (this.rpcMonitor == null) {
            LOG.info("Do not start Router RPC metrics");
        } else {
            this.rpcMonitor.init(this.conf, this, this.router.getStateStore());
        }
        super.serviceInit(configuration);
    }

    @Override
    protected void serviceStart() throws Exception {
        if (this.rpcServer != null) {
            this.rpcServer.start();
            LOG.info("Router RPC up at: {}", this.getRpcAddress());
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        if (this.rpcServer != null) {
            this.rpcServer.stop();
        }
        if (rpcMonitor != null) {
            this.rpcMonitor.close();
        }
        if (securityManager != null) {
            this.securityManager.stop();
        }
        super.serviceStop();
    }


    // 检查操作是否合规
    void checkOperation(OperationCategory op, boolean supported)
            throws StandbyException, UnsupportedOperationException {
        checkOperation(op);

        if (!supported) {
            if (rpcMonitor != null) {
                rpcMonitor.proxyOpNotImplemented();
            }
            String methodName = getMethodName();
            throw new UnsupportedOperationException(
                    "Operation \"" + methodName + "\" is not supported");
        }
    }

    void checkOperation(OperationCategory op)
            throws StandbyException {
        if (rpcMonitor != null) {
            rpcMonitor.startOp();
        }
        if (LOG.isDebugEnabled()) {
            String methodName = getMethodName();
            LOG.debug("Proxying operation: {}", methodName);
        }

        opCategory.set(op);

        // 如果是 UNCHECKED或 READ的话，直接正常返回
        // 否则的话，还得检查下是否处于 safemode [Router]
        if (op == OperationCategory.UNCHECKED || op == OperationCategory.READ) {
            return;
        }
        checkSafeMode();
    }

    private void checkSafeMode() throws StandbyException {
        if (isSafeMode()) {
            // Throw standby exception, router is not available
            if (rpcMonitor != null) {
                rpcMonitor.routerFailureSafemode();
            }
            OperationCategory op = opCategory.get();
            throw new StandbyException("Router " + router.getRouterId() +
                    " is in safe mode and cannot handle " + op + " requests");
        }
    }


    // 优先使用默认的 ns进行调用
    // clazz是返回值类型
    <T> T invokeAtAvailableNs(RemoteMethod method, Class<T> clazz)
            throws IOException {
        String nsId = subclusterResolver.getDefaultNamespace();
        if (!nsId.isEmpty()) {
            return rpcClient.invokeSingle(nsId, method, clazz);
        }

        Set<FederationNamespaceInfo> nss = namenodeResolver.getNamespaces();
        if (nss.isEmpty()) {
            throw new IOException("No namespace available.");
        }
        nsId = nss.iterator().next().getNameserviceId();
        return rpcClient.invokeSingle(nsId, method, clazz);
    }

    @Override // ClientProtocol, 获得 DelegationToken
    public Token<DelegationTokenIdentifier> getDelegationToken(Text renewer)
            throws IOException {
        return clientProto.getDelegationToken(renewer);
    }

    @Override // ClientProtocol，renewDelegationToken
    public long renewDelegationToken(Token<DelegationTokenIdentifier> token)
            throws IOException {
        return clientProto.renewDelegationToken(token);
    }

    @Override // ClientProtocol，销毁DelegationToken
    public void cancelDelegationToken(Token<DelegationTokenIdentifier> token)
            throws IOException {
        clientProto.cancelDelegationToken(token);
    }

    @Override // ClientProtocol，获取 src文件在某一段范围内的 blocks信息
    /*
        其中一条 LocatedBlock：{
        BP-199617693-10.69.75.246-1693976594441:blk_1073749630_8825;
        getBlockSize()=793; corrupt=false; offset=0;
        locs=[
            DatanodeInfoWithStorage[10.5.5.171:50010,DS-674b3994-e6ea-4919-8ade-c6af85798c8b,DISK],
            DatanodeInfoWithStorage[10.5.5.217:50010,DS-78b1dc74-d647-42bb-a524-478acd941fb7,DISK],
            DatanodeInfoWithStorage[10.5.5.20:50010,DS-6fcae861-65b3-4b71-962e-2b7f04faa8ba,DISK]
        ];
        cachedLocs=[]
        }
     */
    public LocatedBlocks getBlockLocations(String src, final long offset,
                                           final long length) throws IOException {
        return clientProto.getBlockLocations(src, offset, length);
    }

    @Override // ClientProtocol，获得服务端的配置值
    public FsServerDefaults getServerDefaults() throws IOException {
        return clientProto.getServerDefaults();
    }

    @Override // ClientProtocol，创建一个文件
    public HdfsFileStatus create(String src, FsPermission masked,
                                 String clientName, EnumSetWritable<CreateFlag> flag,
                                 boolean createParent, short replication, long blockSize,
                                 CryptoProtocolVersion[] supportedVersions, String ecPolicyName,
                                 String storagePolicy)
            throws IOException {
        return clientProto.create(src, masked, clientName, flag, createParent,
                replication, blockSize, supportedVersions, ecPolicyName, storagePolicy);
    }


    // 获取一个文件的 RemoteLocation信息
    RemoteLocation getCreateLocation(final String src) throws IOException {
        // 从挂载表解析器里可以获得一堆 RemoteLocation，
        // 但是还是要再向 namenode getFileInfo校验下，并返回任意一条实际存在的 RemoteLocation即可
        // 但是，RemoteLocation size＞1时才校验，不然则直接返回  为什么?
        final List<RemoteLocation> locations = getLocationsForPath(src, true);
        return getCreateLocation(src, locations);
    }

    RemoteLocation getCreateLocation(final String src, final List<RemoteLocation> locations)
            throws IOException {
        if (locations == null || locations.isEmpty()) {
            throw new IOException("Cannot get locations to create " + src);
        }

        RemoteLocation createLocation = locations.get(0);
        if (locations.size() > 1) {
            try {
                RemoteLocation existingLocation = getExistingLocation(locations);
                if (existingLocation != null) {
                    LOG.debug("{} already exists in {}.", src, existingLocation);
                    createLocation = existingLocation;
                }
            } catch (FileNotFoundException ignored) {
            }
        }
        return createLocation;
    }

    // 向 namenode发起 getFileInfo请求
    private RemoteLocation getExistingLocation(List<RemoteLocation> locations) throws IOException {
        RemoteMethod method = new RemoteMethod("getFileInfo",
                new Class<?>[]{String.class}, new RemoteParam());
        Map<RemoteLocation, HdfsFileStatus> results = rpcClient.invokeConcurrent(
                locations, method, true, false, HdfsFileStatus.class);
        for (RemoteLocation loc : locations) {
            if (results.get(loc) != null) {
                return loc;
            }
        }
        return null;
    }

    @Override // ClientProtocol，append追加数据
    // ? 为什么返回的是 LastBlockWithStatus? 这个和 Router没关系，是 ClienProtol的事
    // 难道靠 LastBlockWithStatus来写数据？
    public LastBlockWithStatus append(String src, final String clientName,
                                      final EnumSetWritable<CreateFlag> flag) throws IOException {
        return clientProto.append(src, clientName, flag);
    }

    @Override // ClientProtocol，恢复租约
    public boolean recoverLease(String src, String clientName)
            throws IOException {
        return clientProto.recoverLease(src, clientName);
    }

    @Override // ClientProtocol，设置某个文件的副本数
    public boolean setReplication(String src, short replication)
            throws IOException {
        return clientProto.setReplication(src, replication);
    }

    @Override // ClientProtocol，设置某个文件的存储策略 (冷存储 还是 热存储)
    public void setStoragePolicy(String src, String policyName)
            throws IOException {
        clientProto.setStoragePolicy(src, policyName);
    }

    @Override // ClientProtocol, 获得全部的存储策略
    // BlockStoragePolicy{COLD:2, storageTypes=[ARCHIVE], creationFallbacks=[], replicationFallbacks=[]},
    // BlockStoragePolicy{WARM:5, storageTypes=[DISK, ARCHIVE], creationFallbacks=[DISK, ARCHIVE], replicationFallbacks=[DISK, ARCHIVE]},
    // BlockStoragePolicy{HOT:7, storageTypes=[DISK], creationFallbacks=[], replicationFallbacks=[ARCHIVE]} ...
    public BlockStoragePolicy[] getStoragePolicies() throws IOException {
        return clientProto.getStoragePolicies();
    }

    @Override // ClientProtocol, 给某个文件设置权限
    public void setPermission(String src, FsPermission permissions)
            throws IOException {
        clientProto.setPermission(src, permissions);
    }

    @Override // ClientProtocol, 给某个文件设置所有者
    public void setOwner(String src, String username, String groupname)
            throws IOException {
        clientProto.setOwner(src, username, groupname);
    }

    @Override // ClientProtocol, 为这个文件新申请一个 block
    // 且 Excluded && favored 的节点会被无视 为啥?
    public LocatedBlock addBlock(String src, String clientName,
                                 ExtendedBlock previous, DatanodeInfo[] excludedNodes, long fileId,
                                 String[] favoredNodes, EnumSet<AddBlockFlag> addBlockFlags)
            throws IOException {
        return clientProto.addBlock(src, clientName, previous, excludedNodes,
                fileId, favoredNodes, addBlockFlags);
    }

    @Override // ClientProtocol，getAdditionalDatanode，
    // 且 DfsClient里没有这个接口 ?
    // Excluded的节点会被无视 为啥?
    public LocatedBlock getAdditionalDatanode(final String src, final long fileId,
                                              final ExtendedBlock blk, final DatanodeInfo[] existings,
                                              final String[] existingStorageIDs, final DatanodeInfo[] excludes,
                                              final int numAdditionalNodes, final String clientName)
            throws IOException {
        return clientProto.getAdditionalDatanode(src, fileId, blk, existings,
                existingStorageIDs, excludes, numAdditionalNodes, clientName);
    }

    @Override // ClientProtocol, 放弃一个 Block
    public void abandonBlock(ExtendedBlock b, long fileId, String src,
                             String holder) throws IOException {
        clientProto.abandonBlock(b, fileId, src, holder);
    }

    @Override // ClientProtocol, 完成这个 Block
    public boolean complete(String src, String clientName, ExtendedBlock last,
                            long fileId) throws IOException {
        return clientProto.complete(src, clientName, last, fileId);
    }

    @Override // ClientProtocol，更新 Block的戳
    public LocatedBlock updateBlockForPipeline(
            ExtendedBlock block, String clientName) throws IOException {
        return clientProto.updateBlockForPipeline(block, clientName);
    }

    // 将 newBlock的一些属性赋值给 oldBlock
    // 还将 newStorageIDs赋值给 oldBlock
    @Override // ClientProtocol，更新 Block信息以及 Block所处的数据流管道
    public void updatePipeline(String clientName, ExtendedBlock oldBlock,
                               ExtendedBlock newBlock, DatanodeID[] newNodes, String[] newStorageIDs)
            throws IOException {
        clientProto.updatePipeline(clientName, oldBlock, newBlock, newNodes,
                newStorageIDs);
    }

    @Override // ClientProtocol, getPreferredBlockSize 获得指定文件的块实际大小
    public long getPreferredBlockSize(String src) throws IOException {
        return clientProto.getPreferredBlockSize(src);
    }

    @Deprecated
    @Override // ClientProtocol，rename改名
    public boolean rename(final String src, final String dst)
            throws IOException {
        return clientProto.rename(src, dst);
    }

    @Override // ClientProtocol，rename2还增加了 options选项, 如已存在的话是否进行覆盖
    public void rename2(final String src, final String dst,
                        final Options.Rename... options) throws IOException {
        clientProto.rename2(src, dst, options);
    }

    @Override // ClientProtocol，连接文件
    public void concat(String trg, String[] src) throws IOException {
        clientProto.concat(trg, src);
    }

    @Override // ClientProtocol，截取文件
    public boolean truncate(String src, long newLength, String clientName)
            throws IOException {
        return clientProto.truncate(src, newLength, clientName);
    }

    @Override // ClientProtocol，删除文件
    public boolean delete(String src, boolean recursive) throws IOException {
        return clientProto.delete(src, recursive);
    }

    @Override // ClientProtocol，创建目录
    public boolean mkdirs(String src, FsPermission masked, boolean createParent)
            throws IOException {
        return clientProto.mkdirs(src, masked, createParent);
    }

    @Override // ClientProtocol，续租
    public void renewLease(String clientName) throws IOException {
        clientProto.renewLease(clientName);
    }

    @Override // ClientProtocol，listPaths
    public DirectoryListing getListing(String src, byte[] startAfter,
                                       boolean needLocation) throws IOException {
        return clientProto.getListing(src, startAfter, needLocation);
    }

    @Override
    public BatchedDirectoryListing getBatchedListing(
            String[] srcs, byte[] startAfter, boolean needLocation) {
        throw new UnsupportedOperationException();
    }

    @Override // ClientProtocol
    public HdfsFileStatus getFileInfo(String src) throws IOException {
        return clientProto.getFileInfo(src);
    }

    @Override // ClientProtocol
    public boolean isFileClosed(String src) throws IOException {
        return clientProto.isFileClosed(src);
    }

    @Override // ClientProtocol
    public HdfsFileStatus getFileLinkInfo(String src) throws IOException {
        return clientProto.getFileLinkInfo(src);
    }

    @Override // ClientProtocol
    public HdfsLocatedFileStatus getLocatedFileInfo(String src,
                                                    boolean needBlockToken) throws IOException {
        return clientProto.getLocatedFileInfo(src, needBlockToken);
    }

    @Override // ClientProtocol
    public long[] getStats() throws IOException {
        return clientProto.getStats();
    }

    @Override // ClientProtocol
    public DatanodeInfo[] getDatanodeReport(DatanodeReportType type)
            throws IOException {
        return clientProto.getDatanodeReport(type);
    }

    // ClientProtocol.getDatanodeReport()，获得指定存活类型的 DataNode
    public DatanodeInfo[] getDatanodeReport(DatanodeReportType type, boolean requireResponse, long timeOutMs)
            throws IOException {
        checkOperation(OperationCategory.UNCHECKED);

        Map<String, DatanodeInfo> datanodesMap = new LinkedHashMap<>();
        RemoteMethod method = new RemoteMethod("getDatanodeReport",
                new Class<?>[]{DatanodeReportType.class}, type);

        Set<FederationNamespaceInfo> nss = namenodeResolver.getNamespaces();
        Map<FederationNamespaceInfo, DatanodeInfo[]> results =
                rpcClient.invokeConcurrent(nss, method, requireResponse, false,
                        timeOutMs, DatanodeInfo[].class);
        for (Entry<FederationNamespaceInfo, DatanodeInfo[]> entry :
                results.entrySet()) {
            FederationNamespaceInfo ns = entry.getKey();
            DatanodeInfo[] result = entry.getValue();
            for (DatanodeInfo node : result) {
                String nodeId = node.getXferAddr();
                DatanodeInfo dn = datanodesMap.get(nodeId);
                if (dn == null || node.getLastUpdate() > dn.getLastUpdate()) {
                    // Add the subcluster as a suffix to the network location
                    node.setNetworkLocation(
                            NodeBase.PATH_SEPARATOR_STR + ns.getNameserviceId() +
                                    node.getNetworkLocation());
                    datanodesMap.put(nodeId, node);
                } else {
                    LOG.debug("{} is in multiple subclusters", nodeId);
                }
            }
        }
        // Map -> Array
        Collection<DatanodeInfo> datanodes = datanodesMap.values();
        return toArray(datanodes, DatanodeInfo.class);
    }

    @Override // ClientProtocol
    public DatanodeStorageReport[] getDatanodeStorageReport(
            DatanodeReportType type) throws IOException {
        return clientProto.getDatanodeStorageReport(type);
    }

    // ns_id -> 所有的DataNode信息
    public Map<String, DatanodeStorageReport[]> getDatanodeStorageReportMap(
            DatanodeReportType type) throws IOException {

        Map<String, DatanodeStorageReport[]> ret = new LinkedHashMap<>();
        RemoteMethod method = new RemoteMethod("getDatanodeStorageReport",
                new Class<?>[]{DatanodeReportType.class}, type);
        Set<FederationNamespaceInfo> nss = namenodeResolver.getNamespaces();
        Map<FederationNamespaceInfo, DatanodeStorageReport[]> results =
                rpcClient.invokeConcurrent(
                        nss, method, true, false, DatanodeStorageReport[].class);
        for (Entry<FederationNamespaceInfo, DatanodeStorageReport[]> entry :
                results.entrySet()) {
            FederationNamespaceInfo ns = entry.getKey();
            String nsId = ns.getNameserviceId();
            DatanodeStorageReport[] result = entry.getValue();
            ret.put(nsId, result);
        }
        return ret;
    }

    @Override // ClientProtocol
    public boolean setSafeMode(SafeModeAction action, boolean isChecked)
            throws IOException {
        return clientProto.setSafeMode(action, isChecked);
    }

    @Override // ClientProtocol
    public boolean restoreFailedStorage(String arg) throws IOException {
        return clientProto.restoreFailedStorage(arg);
    }

    @Override // ClientProtocol
    public boolean saveNamespace(long timeWindow, long txGap) throws IOException {
        return clientProto.saveNamespace(timeWindow, txGap);
    }

    @Override // ClientProtocol
    public long rollEdits() throws IOException {
        return clientProto.rollEdits();
    }

    @Override // ClientProtocol
    public void refreshNodes() throws IOException {
        clientProto.refreshNodes();
    }

    @Override // ClientProtocol
    public void finalizeUpgrade() throws IOException {
        clientProto.finalizeUpgrade();
    }

    @Override // ClientProtocol
    public boolean upgradeStatus() throws IOException {
        return clientProto.upgradeStatus();
    }

    @Override // ClientProtocol
    public RollingUpgradeInfo rollingUpgrade(RollingUpgradeAction action)
            throws IOException {
        return clientProto.rollingUpgrade(action);
    }

    @Override // ClientProtocol
    public void metaSave(String filename) throws IOException {
        clientProto.metaSave(filename);
    }

    @Override // ClientProtocol
    public CorruptFileBlocks listCorruptFileBlocks(String path, String cookie)
            throws IOException {
        return clientProto.listCorruptFileBlocks(path, cookie);
    }

    @Override // ClientProtocol
    public void setBalancerBandwidth(long bandwidth) throws IOException {
        clientProto.setBalancerBandwidth(bandwidth);
    }

    @Override // ClientProtocol
    public ContentSummary getContentSummary(String path) throws IOException {
        return clientProto.getContentSummary(path);
    }

    @Override // ClientProtocol
    public void fsync(String src, long fileId, String clientName,
                      long lastBlockLength) throws IOException {
        clientProto.fsync(src, fileId, clientName, lastBlockLength);
    }

    @Override // ClientProtocol
    public void setTimes(String src, long mtime, long atime) throws IOException {
        clientProto.setTimes(src, mtime, atime);
    }

    @Override // ClientProtocol
    public void createSymlink(String target, String link, FsPermission dirPerms,
                              boolean createParent) throws IOException {
        clientProto.createSymlink(target, link, dirPerms, createParent);
    }

    @Override // ClientProtocol
    public String getLinkTarget(String path) throws IOException {
        return clientProto.getLinkTarget(path);
    }

    @Override // ClientProtocol
    public void allowSnapshot(String snapshotRoot) throws IOException {
        clientProto.allowSnapshot(snapshotRoot);
    }

    @Override // ClientProtocol
    public void disallowSnapshot(String snapshot) throws IOException {
        clientProto.disallowSnapshot(snapshot);
    }

    @Override // ClientProtocol
    public void renameSnapshot(String snapshotRoot, String snapshotOldName,
                               String snapshotNewName) throws IOException {
        clientProto.renameSnapshot(snapshotRoot, snapshotOldName, snapshotNewName);
    }

    @Override // ClientProtocol
    public SnapshottableDirectoryStatus[] getSnapshottableDirListing()
            throws IOException {
        return clientProto.getSnapshottableDirListing();
    }

    @Override // ClientProtocol
    public SnapshotDiffReport getSnapshotDiffReport(String snapshotRoot,
                                                    String earlierSnapshotName, String laterSnapshotName) throws IOException {
        return clientProto.getSnapshotDiffReport(
                snapshotRoot, earlierSnapshotName, laterSnapshotName);
    }

    @Override // ClientProtocol
    public SnapshotDiffReportListing getSnapshotDiffReportListing(
            String snapshotRoot, String earlierSnapshotName, String laterSnapshotName,
            byte[] startPath, int index) throws IOException {
        return clientProto.getSnapshotDiffReportListing(snapshotRoot,
                earlierSnapshotName, laterSnapshotName, startPath, index);
    }

    @Override // ClientProtocol
    public long addCacheDirective(CacheDirectiveInfo path,
                                  EnumSet<CacheFlag> flags) throws IOException {
        return clientProto.addCacheDirective(path, flags);
    }

    @Override // ClientProtocol
    public void modifyCacheDirective(CacheDirectiveInfo directive,
                                     EnumSet<CacheFlag> flags) throws IOException {
        clientProto.modifyCacheDirective(directive, flags);
    }

    @Override // ClientProtocol
    public void removeCacheDirective(long id) throws IOException {
        clientProto.removeCacheDirective(id);
    }

    @Override // ClientProtocol
    public BatchedEntries<CacheDirectiveEntry> listCacheDirectives(
            long prevId, CacheDirectiveInfo filter) throws IOException {
        return clientProto.listCacheDirectives(prevId, filter);
    }

    @Override // ClientProtocol
    public void addCachePool(CachePoolInfo info) throws IOException {
        clientProto.addCachePool(info);
    }

    @Override // ClientProtocol
    public void modifyCachePool(CachePoolInfo info) throws IOException {
        clientProto.modifyCachePool(info);
    }

    @Override // ClientProtocol
    public void removeCachePool(String cachePoolName) throws IOException {
        clientProto.removeCachePool(cachePoolName);
    }

    @Override // ClientProtocol
    public BatchedEntries<CachePoolEntry> listCachePools(String prevKey)
            throws IOException {
        return clientProto.listCachePools(prevKey);
    }

    @Override // ClientProtocol
    public void modifyAclEntries(String src, List<AclEntry> aclSpec)
            throws IOException {
        clientProto.modifyAclEntries(src, aclSpec);
    }

    @Override // ClienProtocol
    public void removeAclEntries(String src, List<AclEntry> aclSpec)
            throws IOException {
        clientProto.removeAclEntries(src, aclSpec);
    }

    @Override // ClientProtocol
    public void removeDefaultAcl(String src) throws IOException {
        clientProto.removeDefaultAcl(src);
    }

    @Override // ClientProtocol
    public void removeAcl(String src) throws IOException {
        clientProto.removeAcl(src);
    }

    @Override // ClientProtocol
    public void setAcl(String src, List<AclEntry> aclSpec) throws IOException {
        clientProto.setAcl(src, aclSpec);
    }

    @Override // ClientProtocol
    public AclStatus getAclStatus(String src) throws IOException {
        return clientProto.getAclStatus(src);
    }

    @Override // ClientProtocol
    public void createEncryptionZone(String src, String keyName)
            throws IOException {
        clientProto.createEncryptionZone(src, keyName);
    }

    @Override // ClientProtocol
    public EncryptionZone getEZForPath(String src) throws IOException {
        return clientProto.getEZForPath(src);
    }

    @Override // ClientProtocol
    public BatchedEntries<EncryptionZone> listEncryptionZones(long prevId)
            throws IOException {
        return clientProto.listEncryptionZones(prevId);
    }

    @Override // ClientProtocol
    public void reencryptEncryptionZone(String zone, ReencryptAction action)
            throws IOException {
        clientProto.reencryptEncryptionZone(zone, action);
    }

    @Override // ClientProtocol
    public BatchedEntries<ZoneReencryptionStatus> listReencryptionStatus(
            long prevId) throws IOException {
        return clientProto.listReencryptionStatus(prevId);
    }

    @Override // ClientProtocol
    public void setXAttr(String src, XAttr xAttr, EnumSet<XAttrSetFlag> flag)
            throws IOException {
        clientProto.setXAttr(src, xAttr, flag);
    }

    @Override // ClientProtocol
    public List<XAttr> getXAttrs(String src, List<XAttr> xAttrs)
            throws IOException {
        return clientProto.getXAttrs(src, xAttrs);
    }

    @Override // ClientProtocol
    public List<XAttr> listXAttrs(String src) throws IOException {
        return clientProto.listXAttrs(src);
    }

    @Override // ClientProtocol
    public void removeXAttr(String src, XAttr xAttr) throws IOException {
        clientProto.removeXAttr(src, xAttr);
    }

    @Override // ClientProtocol
    public void checkAccess(String path, FsAction mode) throws IOException {
        clientProto.checkAccess(path, mode);
    }

    @Override // ClientProtocol
    public long getCurrentEditLogTxid() throws IOException {
        return clientProto.getCurrentEditLogTxid();
    }

    @Override // ClientProtocol
    public EventBatchList getEditsFromTxid(long txid) throws IOException {
        return clientProto.getEditsFromTxid(txid);
    }

    @Override // ClientProtocol
    public DataEncryptionKey getDataEncryptionKey() throws IOException {
        return clientProto.getDataEncryptionKey();
    }

    @Override // ClientProtocol
    public String createSnapshot(String snapshotRoot, String snapshotName)
            throws IOException {
        return clientProto.createSnapshot(snapshotRoot, snapshotName);
    }

    @Override // ClientProtocol
    public void deleteSnapshot(String snapshotRoot, String snapshotName)
            throws IOException {
        clientProto.deleteSnapshot(snapshotRoot, snapshotName);
    }

    @Override // ClientProtocol
    public void setQuota(String path, long namespaceQuota, long storagespaceQuota,
                         StorageType type) throws IOException {
        clientProto.setQuota(path, namespaceQuota, storagespaceQuota, type);
    }

    @Override // ClientProtocol
    public QuotaUsage getQuotaUsage(String path) throws IOException {
        return clientProto.getQuotaUsage(path);
    }

    @Override // ClientProtocol
    public void reportBadBlocks(LocatedBlock[] blocks) throws IOException {
        clientProto.reportBadBlocks(blocks);
    }

    @Override // ClientProtocol
    public void unsetStoragePolicy(String src) throws IOException {
        clientProto.unsetStoragePolicy(src);
    }

    @Override // ClientProtocol
    public BlockStoragePolicy getStoragePolicy(String path) throws IOException {
        return clientProto.getStoragePolicy(path);
    }

    @Override // ClientProtocol
    public ErasureCodingPolicyInfo[] getErasureCodingPolicies()
            throws IOException {
        return clientProto.getErasureCodingPolicies();
    }

    @Override // ClientProtocol
    public Map<String, String> getErasureCodingCodecs() throws IOException {
        return clientProto.getErasureCodingCodecs();
    }

    @Override // ClientProtocol
    public AddErasureCodingPolicyResponse[] addErasureCodingPolicies(
            ErasureCodingPolicy[] policies) throws IOException {
        return clientProto.addErasureCodingPolicies(policies);
    }

    @Override // ClientProtocol
    public void removeErasureCodingPolicy(String ecPolicyName)
            throws IOException {
        clientProto.removeErasureCodingPolicy(ecPolicyName);
    }

    @Override // ClientProtocol
    public void disableErasureCodingPolicy(String ecPolicyName)
            throws IOException {
        clientProto.disableErasureCodingPolicy(ecPolicyName);
    }

    @Override // ClientProtocol
    public void enableErasureCodingPolicy(String ecPolicyName)
            throws IOException {
        clientProto.enableErasureCodingPolicy(ecPolicyName);
    }

    @Override // ClientProtocol
    public ErasureCodingPolicy getErasureCodingPolicy(String src)
            throws IOException {
        return clientProto.getErasureCodingPolicy(src);
    }

    @Override // ClientProtocol
    public void setErasureCodingPolicy(String src, String ecPolicyName)
            throws IOException {
        clientProto.setErasureCodingPolicy(src, ecPolicyName);
    }

    @Override // ClientProtocol
    public void unsetErasureCodingPolicy(String src) throws IOException {
        clientProto.unsetErasureCodingPolicy(src);
    }

    @Override
    public ECTopologyVerifierResult getECTopologyResultForPolicies(
            String... policyNames) throws IOException {
        return clientProto.getECTopologyResultForPolicies(policyNames);
    }

    @Override // ClientProtocol
    public ECBlockGroupStats getECBlockGroupStats() throws IOException {
        return clientProto.getECBlockGroupStats();
    }

    @Override // ClientProtocol
    public ReplicatedBlockStats getReplicatedBlockStats() throws IOException {
        return clientProto.getReplicatedBlockStats();
    }

    @Deprecated
    @Override // ClientProtocol
    public BatchedEntries<OpenFileEntry> listOpenFiles(long prevId)
            throws IOException {
        return clientProto.listOpenFiles(prevId);
    }

    @Override // ClientProtocol
    public HAServiceProtocol.HAServiceState getHAServiceState() {
        return clientProto.getHAServiceState();
    }

    @Override // ClientProtocol
    public BatchedEntries<OpenFileEntry> listOpenFiles(long prevId,
                                                       EnumSet<OpenFilesType> openFilesTypes, String path) throws IOException {
        return clientProto.listOpenFiles(prevId, openFilesTypes, path);
    }

    @Override // ClientProtocol
    public void msync() throws IOException {
        clientProto.msync();
    }

    @Override // ClientProtocol
    public void satisfyStoragePolicy(String path) throws IOException {
        clientProto.satisfyStoragePolicy(path);
    }

    @Override // NamenodeProtocol
    public BlocksWithLocations getBlocks(DatanodeInfo datanode, long size,
                                         long minBlockSize) throws IOException {
        return nnProto.getBlocks(datanode, size, minBlockSize);
    }

    @Override // NamenodeProtocol
    public ExportedBlockKeys getBlockKeys() throws IOException {
        return nnProto.getBlockKeys();
    }

    @Override // NamenodeProtocol
    public long getTransactionID() throws IOException {
        return nnProto.getTransactionID();
    }

    @Override // NamenodeProtocol
    public long getMostRecentCheckpointTxId() throws IOException {
        return nnProto.getMostRecentCheckpointTxId();
    }

    @Override // NamenodeProtocol
    public CheckpointSignature rollEditLog() throws IOException {
        return nnProto.rollEditLog();
    }

    @Override // NamenodeProtocol
    public NamespaceInfo versionRequest() throws IOException {
        return nnProto.versionRequest();
    }

    @Override // NamenodeProtocol
    public void errorReport(NamenodeRegistration registration, int errorCode,
                            String msg) throws IOException {
        nnProto.errorReport(registration, errorCode, msg);
    }

    @Override // NamenodeProtocol
    public NamenodeRegistration registerSubordinateNamenode(
            NamenodeRegistration registration) throws IOException {
        return nnProto.registerSubordinateNamenode(registration);
    }

    @Override // NamenodeProtocol
    public NamenodeCommand startCheckpoint(NamenodeRegistration registration)
            throws IOException {
        return nnProto.startCheckpoint(registration);
    }

    @Override // NamenodeProtocol
    public void endCheckpoint(NamenodeRegistration registration,
                              CheckpointSignature sig) throws IOException {
        nnProto.endCheckpoint(registration, sig);
    }

    @Override // NamenodeProtocol
    public RemoteEditLogManifest getEditLogManifest(long sinceTxId)
            throws IOException {
        return nnProto.getEditLogManifest(sinceTxId);
    }

    @Override // NamenodeProtocol
    public boolean isUpgradeFinalized() throws IOException {
        return nnProto.isUpgradeFinalized();
    }

    @Override // NamenodeProtocol
    public boolean isRollingUpgrade() throws IOException {
        return nnProto.isRollingUpgrade();
    }

    @Override // NamenodeProtocol
    public Long getNextSPSPath() throws IOException {
        return nnProto.getNextSPSPath();
    }

    // 根据 path 和 bp_id 获取一个可用的 RemoteLocation
    //    其实是先直接获得了 RemoteLocations，然后根据 bp_id找出对应的 ns
    //    然后再遍历所有的 RemoteLocations，只要有一个 RemoteLocation的 ns满足，则直接 return
    protected RemoteLocation getLocationForPath(String path, String blockPoolId)
            throws IOException {

        final List<RemoteLocation> locations = getLocationsForPath(path, true);

        String nameserviceId = null;
        Set<FederationNamespaceInfo> namespaces = this.namenodeResolver.getNamespaces();
        for (FederationNamespaceInfo namespace : namespaces) {
            if (namespace.getBlockPoolId().equals(blockPoolId)) {
                nameserviceId = namespace.getNameserviceId();
                break;
            }
        }
        if (nameserviceId != null) {
            for (RemoteLocation location : locations) {
                if (location.getNameserviceId().equals(nameserviceId)) {
                    return location;
                }
            }
        }
        throw new IOException("Cannot locate a nameservice for block pool " + blockPoolId);
    }

    // 根据 path获得所有 RemoteLocations
    protected List<RemoteLocation> getLocationsForPath(String path,
                                                       boolean failIfLocked) throws IOException {
        return getLocationsForPath(path, failIfLocked, true);
    }

    // failIfLocked时，如果该 path存在孩子挂载点，则认为失败
    protected List<RemoteLocation> getLocationsForPath(String path,
                                                       boolean failIfLocked, boolean needQuotaVerify) throws IOException {
        try {
            if (failIfLocked) {
                final List<String> mountPoints = this.subclusterResolver.getMountPoints(path);
                if (mountPoints != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The operation is not allowed because ");
                    if (mountPoints.isEmpty()) {
                        sb.append("the path: ")
                                .append(path)
                                .append(" is a mount point");
                    } else {
                        sb.append("there are mount points: ")
                                .append(String.join(",", mountPoints))
                                .append(" under the path: ")
                                .append(path);
                    }
                    throw new AccessControlException(sb.toString());
                }
            }

            // 这里已经取得了所有的 RemoteLocations, 下面再按禁用 ns过滤一遍
            final PathLocation location = this.subclusterResolver.getDestinationForPath(path);
            if (location == null) {
                throw new IOException("Cannot find locations for " + path + " in " +
                        this.subclusterResolver.getClass().getSimpleName());
            }

            if (opCategory.get() == OperationCategory.WRITE) {
                // 检查是否为 "只读"挂载点
                if (isPathReadOnly(path)) {
                    if (this.rpcMonitor != null) {
                        this.rpcMonitor.routerFailureReadOnly();
                    }
                    throw new IOException(path + " is in a read only mount point");
                }

                // 检查 Quota
                if (this.router.isQuotaEnabled() && needQuotaVerify) {
                    RouterQuotaUsage quotaUsage = this.router.getQuotaManager().getQuotaUsage(path);
                    if (quotaUsage != null) {
                        quotaUsage.verifyNamespaceQuota();
                        quotaUsage.verifyStoragespaceQuota();
                        quotaUsage.verifyQuotaByStorageType();
                    }
                }
            }

            // 过滤掉那些被禁用的 ns
            Set<String> disabled = namenodeResolver.getDisabledNamespaces();
            List<RemoteLocation> locs = new ArrayList<>();
            for (RemoteLocation loc : location.getDestinations()) {
                if (!disabled.contains(loc.getNameserviceId())) {
                    locs.add(loc);
                }
            }
            return locs;
        } catch (IOException ioe) {
            if (this.rpcMonitor != null) {
                this.rpcMonitor.routerFailureStateStore();
            }
            if (ioe instanceof StateStoreUnavailableException) {
                checkSafeMode();
            }
            throw ioe;
        }
    }

    // 检查这个文件的挂载点是否为 "只读"
    private boolean isPathReadOnly(final String path) {
        if (subclusterResolver instanceof MountTableResolver) {
            try {
                MountTableResolver mountTable = (MountTableResolver) subclusterResolver;
                MountTable entry = mountTable.getMountPoint(path);
                if (entry != null && entry.isReadOnly()) {
                    return true;
                }
            } catch (IOException e) {
                LOG.error("Cannot get mount point", e);
            }
        }
        return false;
    }

    // CUR_USER -> Server.getRemoteUser() -> UserGroupInformation.getCurrentUser() 的降级顺序
    public static UserGroupInformation getRemoteUser() throws IOException {
        UserGroupInformation ugi = CUR_USER.get();
        ugi = (ugi != null) ? ugi : Server.getRemoteUser();
        return (ugi != null) ? ugi : UserGroupInformation.getCurrentUser();
    }


    // 即获得 map.values()
    static <T> T[] merge(Map<FederationNamespaceInfo, T[]> map, Class<T> clazz) {

        // Put all results into a set to avoid repeats
        Set<T> ret = new LinkedHashSet<>();
        for (T[] values : map.values()) {
            if (values != null) {
                ret.addAll(Arrays.asList(values));
            }
        }
        return toArray(ret, clazz);
    }

    // 检查 path是否属于所有的子集群，
    //   当能在挂载表中查到 && 负载均衡策略设置为(SPACE, RANDOM, HASH_ALL) 时 才返回 true
    boolean isPathAll(final String path) {
        if (subclusterResolver instanceof MountTableResolver) {
            try {
                MountTableResolver mountTable = (MountTableResolver) subclusterResolver;
                MountTable entry = mountTable.getMountPoint(path);
                if (entry != null) {
                    return entry.isAll();
                }
            } catch (IOException e) {
                LOG.error("Cannot get mount point", e);
            }
        }
        return false;
    }

    // 判断这个挂载点是否支持容错，降级到其他 ns
    boolean isPathFaultTolerant(final String path) {
        if (subclusterResolver instanceof MountTableResolver) {
            try {
                MountTableResolver mountTable = (MountTableResolver) subclusterResolver;
                MountTable entry = mountTable.getMountPoint(path);
                if (entry != null) {
                    return entry.isFaultTolerant();
                }
            } catch (IOException e) {
                LOG.error("Cannot get mount point", e);
            }
        }
        return false;
    }

    // 是否应该在所有 RemoteLocations中调用
    boolean isInvokeConcurrent(final String path) throws IOException {
        if (subclusterResolver instanceof MountTableResolver) {
            MountTableResolver mountTableResolver = (MountTableResolver) subclusterResolver;
            List<String> mountPoints = mountTableResolver.getMountPoints(path);
            // 存在 path孩子挂载点 就返回true
            if (mountPoints != null) {
                return true;
            }
            return isPathAll(path);
        }
        return false;
    }

    @Override
    public void refreshUserToGroupsMappings() throws IOException {
        routerProto.refreshUserToGroupsMappings();
    }

    @Override
    public void refreshSuperUserGroupsConfiguration() throws IOException {
        routerProto.refreshSuperUserGroupsConfiguration();
    }

    @Override
    public String[] getGroupsForUser(String user) throws IOException {
        return routerProto.getGroupsForUser(user);
    }


    /*
        get/set/简单 方法
     */

    public RouterSecurityManager getRouterSecurityManager() {
        return this.securityManager;
    }

    public RouterRpcClient getRPCClient() {
        return rpcClient;
    }

    public ActiveNamenodeResolver getNamenodeResolver() {
        return namenodeResolver;
    }

    public FileSubclusterResolver getSubclusterResolver() {
        return subclusterResolver;
    }

    @VisibleForTesting
    public Server getServer() {
        return rpcServer;
    }

    public InetSocketAddress getRpcAddress() {
        return rpcAddress;
    }

    boolean isSafeMode() {
        RouterSafemodeService safemodeService = router.getSafemodeService();
        return (safemodeService != null && safemodeService.isInSafeMode());
    }

    static String getMethodName() {
        final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        return stack[3].getMethodName();
    }

    static void setCurrentUser(UserGroupInformation ugi) {
        CUR_USER.set(ugi);
    }

    static void resetCurrentUser() {
        CUR_USER.set(null);
    }

    static <T> T[] toArray(Collection<T> set, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] combinedData = (T[]) Array.newInstance(clazz, set.size());
        combinedData = set.toArray(combinedData);
        return combinedData;
    }

    public Quota getQuotaModule() {
        return this.quotaCall;
    }

}
