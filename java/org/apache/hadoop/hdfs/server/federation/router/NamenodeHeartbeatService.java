package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ha.HAServiceProtocol;
import org.apache.hadoop.ha.HAServiceStatus;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.NameNodeProxies;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.server.federation.resolver.ActiveNamenodeResolver;
import org.apache.hadoop.hdfs.server.federation.resolver.NamenodeStatusReport;
import org.apache.hadoop.hdfs.server.protocol.NamenodeProtocol;
import org.apache.hadoop.hdfs.server.protocol.NamespaceInfo;
import org.apache.hadoop.hdfs.tools.DFSHAAdmin;
import org.apache.hadoop.hdfs.tools.NNHAServiceTarget;
import org.apache.hadoop.hdfs.web.URLConnectionFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

import static org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys.DFS_ROUTER_HEARTBEAT_INTERVAL_MS;
import static org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys.DFS_ROUTER_HEARTBEAT_INTERVAL_MS_DEFAULT;

// 定期性汇报心跳, 更新 NameNode的 HA状态以及 可用空间等状态
public class NamenodeHeartbeatService extends PeriodicService {

    private static final Logger LOG = LoggerFactory.getLogger(NamenodeHeartbeatService.class);

    private Configuration conf;

    // ActiveNamenodeResolver
    private final ActiveNamenodeResolver resolver;

    private final String nameserviceId;
    private final String namenodeId;

    private NNHAServiceTarget localTarget;
    // 获取 HA状态的客户端
    private HAServiceProtocol localTargetHAProtocol;

    // address相关
    private String rpcAddress;
    private String serviceAddress;
    private String lifelineAddress;
    private String webAddress;

    // jmx相关
    private URLConnectionFactory connectionFactory;
    private String scheme;  // "http","https"

    public NamenodeHeartbeatService(ActiveNamenodeResolver resolver, String nsId, String nnId) {
        super(NamenodeHeartbeatService.class.getSimpleName() +
                (nsId == null ? "" : " " + nsId) +
                (nnId == null ? "" : " " + nnId));
        this.resolver = resolver;
        this.nameserviceId = nsId;
        this.namenodeId = nnId;
    }

    @Override
    protected void serviceInit(Configuration configuration) throws Exception {
        this.conf = DFSHAAdmin.addSecurityConfiguration(configuration);
        String nnDesc = nameserviceId;
        if (this.namenodeId != null && !this.namenodeId.isEmpty()) {
            this.localTarget = new NNHAServiceTarget(
                    conf, nameserviceId, namenodeId);
            nnDesc += "-" + namenodeId;
        } else {
            this.localTarget = null;
        }
        this.rpcAddress = getRpcAddress(conf, nameserviceId, namenodeId);
        LOG.info("{} RPC address: {}", nnDesc, rpcAddress);
        this.serviceAddress = DFSUtil.getNamenodeServiceAddr(conf, nameserviceId, namenodeId);
        if (this.serviceAddress == null) {
            LOG.error("Cannot locate RPC service address for NN {}, " +
                    "using RPC address {}", nnDesc, this.rpcAddress);
            this.serviceAddress = this.rpcAddress;
        }
        LOG.info("{} Service RPC address: {}", nnDesc, serviceAddress);
        this.lifelineAddress = DFSUtil.getNamenodeLifelineAddr(conf, nameserviceId, namenodeId);
        if (this.lifelineAddress == null) {
            this.lifelineAddress = this.serviceAddress;
        }
        LOG.info("{} Lifeline RPC address: {}", nnDesc, lifelineAddress);
        this.webAddress = DFSUtil.getNamenodeWebAddr(conf, nameserviceId, namenodeId);
        LOG.info("{} Web address: {}", nnDesc, webAddress);
        this.connectionFactory = URLConnectionFactory.newDefaultURLConnectionFactory(conf);
        this.scheme = DFSUtil.getHttpPolicy(conf).isHttpEnabled() ? "http" : "https";

        this.setIntervalMs(conf.getLong(DFS_ROUTER_HEARTBEAT_INTERVAL_MS, DFS_ROUTER_HEARTBEAT_INTERVAL_MS_DEFAULT));
        super.serviceInit(configuration);
    }


    private static String getRpcAddress(Configuration conf, String nsId, String nnId) {
        String confKey = DFSConfigKeys.DFS_NAMENODE_RPC_ADDRESS_KEY;
        String ret = conf.get(confKey);
        if (nsId != null || nnId != null) {
            confKey = DFSUtil.addKeySuffixes(confKey, nsId, nnId);
            ret = conf.get(confKey);
            if (ret == null) {
                Map<String, InetSocketAddress> rpcAddresses =
                        DFSUtil.getRpcAddressesForNameserviceId(conf, nsId, null);
                InetSocketAddress sockAddr = null;
                if (nnId != null) {
                    sockAddr = rpcAddresses.get(nnId);
                } else if (rpcAddresses.size() == 1) {
                    sockAddr = rpcAddresses.values().iterator().next();
                }
                if (sockAddr != null) {
                    InetAddress addr = sockAddr.getAddress();
                    ret = addr.getHostName() + ":" + sockAddr.getPort();
                }
            }
        }
        return ret;
    }

    @Override
    public void periodicInvoke() {
        updateState();
    }

    // getNamenodeStatusReport(), 然后 resolver.registerNamenode(report), 最终更新到 ZNode
    private void updateState() {
        NamenodeStatusReport report = getNamenodeStatusReport();
        if (!report.registrationValid()) {
            LOG.error("Namenode is not operational: {}", getNamenodeDesc());
        } else if (report.haStateValid()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received service state: {} from HA namenode: {}", report.getState(), getNamenodeDesc());
            }
        } else if (localTarget == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reporting non-HA namenode as operational: {}", getNamenodeDesc());
            }
        } else {
            return;
        }

        try {
            if (!resolver.registerNamenode(report)) {
                LOG.warn("Cannot register namenode {}", report);
            }
        } catch (IOException e) {
            LOG.info("Cannot register namenode in the State Store");
        } catch (Exception ex) {
            LOG.error("Unhandled exception updating NN registration for {}",
                    getNamenodeDesc(), ex);
        }
    }

    // 获得 NameNode信息
    protected NamenodeStatusReport getNamenodeStatusReport() {
        NamenodeStatusReport report = new NamenodeStatusReport(nameserviceId,
                namenodeId, rpcAddress, serviceAddress,
                lifelineAddress, scheme, webAddress);
        try {
            LOG.debug("Probing NN at service address: {}", serviceAddress);
            URI serviceURI = new URI("hdfs://" + serviceAddress);

            // SecondaryNN的协议, 
            // 创建一个伪装成 SecondaryNN的客户端, 用来获取 namenode的 NamespaceInfo
            NamenodeProtocol nn = NameNodeProxies.createProxy(this.conf, serviceURI, NamenodeProtocol.class).getProxy();
            if (nn != null) {
                NamespaceInfo info = nn.versionRequest();
                if (info != null) {
                    report.setNamespaceInfo(info);
                }
            }
            if (!report.registrationValid()) {
                return report;
            }

            // 创建一个普通Client, 去获取 namenode的 safemode状态
            try {
                ClientProtocol client = NameNodeProxies.createProxy(this.conf, serviceURI, ClientProtocol.class).getProxy();
                if (client != null) {
                    boolean isSafeMode = client.setSafeMode(
                            SafeModeAction.SAFEMODE_GET, false);
                    report.setSafeMode(isSafeMode);
                }
            } catch (Exception e) {
                LOG.error("Cannot fetch safemode state for {}", getNamenodeDesc(), e);
            }
            // 赋值 jmx信息
            updateJMXParameters(webAddress, report);

            // 创建一个 HAClient, 获取 HA状态信息
            if (localTarget != null) {
                try {
                    if (localTargetHAProtocol == null) {
                        localTargetHAProtocol = localTarget.getProxy(conf, 30 * 1000);
                    }
                    HAServiceStatus status = localTargetHAProtocol.getServiceStatus();
                    report.setHAServiceState(status.getState());
                } catch (Throwable e) {
                    if (e.getMessage().startsWith("HA for namenode is not enabled")) {
                        LOG.error("HA for {} is not enabled", getNamenodeDesc());
                        localTarget = null;
                    } else {
                        LOG.error("Cannot fetch HA status for {}: {}", getNamenodeDesc(), e.getMessage(), e);
                    }
                    localTargetHAProtocol = null;
                }
            }
        } catch (IOException e) {
            LOG.error("Cannot communicate with {}: {}", getNamenodeDesc(), e.getMessage());
        } catch (Throwable e) {
            LOG.error("Unexpected exception while communicating with {}: {}", getNamenodeDesc(), e.getMessage(), e);
        }
        return report;
    }

    // 赋值 namenodeStatusReport中 jmx部分的信息
    private void updateJMXParameters(String address, NamenodeStatusReport report) {
        try {
            String query = "Hadoop:service=NameNode,name=FSNamesystem*";
            JSONArray aux = FederationUtil.getJmx(query, address, connectionFactory, scheme);
            if (aux != null) {
                for (int i = 0; i < aux.length(); i++) {
                    JSONObject jsonObject = aux.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    if (name.equals("Hadoop:service=NameNode,name=FSNamesystemState")) {
                        report.setDatanodeInfo(
                                jsonObject.getInt("NumLiveDataNodes"),
                                jsonObject.getInt("NumDeadDataNodes"),
                                jsonObject.getInt("NumStaleDataNodes"),
                                jsonObject.getInt("NumDecommissioningDataNodes"),
                                jsonObject.getInt("NumDecomLiveDataNodes"),
                                jsonObject.getInt("NumDecomDeadDataNodes"),
                                jsonObject.optInt("NumInMaintenanceLiveDataNodes"),
                                jsonObject.optInt("NumInMaintenanceDeadDataNodes"),
                                jsonObject.optInt("NumEnteringMaintenanceDataNodes"));
                    } else if (name.equals("Hadoop:service=NameNode,name=FSNamesystem")) {
                        report.setNamesystemInfo(
                                jsonObject.getLong("CapacityRemaining"),
                                jsonObject.getLong("CapacityTotal"),
                                jsonObject.getLong("FilesTotal"),
                                jsonObject.getLong("BlocksTotal"),
                                jsonObject.getLong("MissingBlocks"),
                                jsonObject.getLong("PendingReplicationBlocks"),
                                jsonObject.getLong("UnderReplicatedBlocks"),
                                jsonObject.getLong("PendingDeletionBlocks"),
                                jsonObject.optLong("ProvidedCapacityTotal"));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot get stat from {} using JMX", getNamenodeDesc(), e);
        }
    }

    public String getNamenodeDesc() {
        if (namenodeId != null && !namenodeId.isEmpty()) {
            return nameserviceId + "-" + namenodeId + ":" + serviceAddress;
        } else {
            return nameserviceId + ":" + serviceAddress;
        }
    }
    
    @Override
    protected void serviceStop() throws Exception {
        LOG.info("Stopping NamenodeHeartbeat service for, NS {} NN {} ",
                this.nameserviceId, this.namenodeId);
        if (this.connectionFactory != null) {
            this.connectionFactory.destroy();
        }
        super.serviceStop();
    }
}