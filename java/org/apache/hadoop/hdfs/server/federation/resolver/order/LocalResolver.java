package org.apache.hadoop.hdfs.server.federation.resolver.order;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.hdfs.server.federation.resolver.PathLocation;
import org.apache.hadoop.hdfs.server.federation.router.Router;
import org.apache.hadoop.hdfs.server.federation.router.RouterRpcServer;
import org.apache.hadoop.hdfs.server.federation.store.MembershipStore;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamenodeRegistrationsRequest;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamenodeRegistrationsResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.hdfs.server.protocol.DatanodeStorageReport;
import org.apache.hadoop.ipc.RPC.Server;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.thirdparty.com.google.common.net.HostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LocalResolver extends RouterResolver<String, String> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalResolver.class);

    public LocalResolver(final Configuration conf, final Router routerService) {
        super(conf, routerService);
    }


    // 核心方法实现, 获得最优的 ns
    // 即 getRemoteAddress()，获得的是 remote的客户端地址，相对于客户端来说是本地
    @Override
    protected String chooseFirstNamespace(String path, PathLocation loc) {
        String localSubcluster = null;
        String clientAddr = getClientAddr();
        Map<String, String> subclusterInfo = getSubclusterMapping();
        if (subclusterInfo != null) {
            localSubcluster = subclusterInfo.get(clientAddr);
            if (localSubcluster != null) {
                LOG.debug("Local namespace for {} is {}", clientAddr, localSubcluster);
            } else {
                LOG.error("Cannot get local namespace for {}", clientAddr);
            }
        } else {
            LOG.error("Cannot get node mapping when resolving {} at {} from {}", path, loc, clientAddr);
        }
        return localSubcluster;
    }

    @VisibleForTesting
    String getClientAddr() {
        return Server.getRemoteAddress();
    }

    // 获得一个 Map：ip -> ns
    // 既有 dn_ip, 也有 nn_ip
    @Override
    protected Map<String, String> getSubclusterInfo(
            MembershipStore membershipStore) {
        Map<String, String> mapping = new HashMap<>();

        Map<String, String> dnSubCluster = getDatanodesSubcluster();
        if (dnSubCluster != null) {
            mapping.putAll(dnSubCluster);
        }

        Map<String, String> nnSubCluster = getNamenodesSubcluster(membershipStore);
        mapping.putAll(nnSubCluster);
        return mapping;
    }

    // 从 rpcServer.getDatanodeStorageReportMap(...) 获取 dn_ip -> ns
    private Map<String, String> getDatanodesSubcluster() {

        final RouterRpcServer rpcServer = getRpcServer();
        if (rpcServer == null) {
            LOG.error("Cannot access the Router RPC server");
            return null;
        }

        Map<String, String> ret = new HashMap<>();
        try {
            // 使用真实用户的身份 去获取 DatanodeStorageReportMap信息
            UserGroupInformation loginUser = UserGroupInformation.getLoginUser();
            Map<String, DatanodeStorageReport[]> dnMap = loginUser.doAs(
                    new PrivilegedAction<Map<String, DatanodeStorageReport[]>>() {
                        @Override
                        public Map<String, DatanodeStorageReport[]> run() {
                            try {
                                return rpcServer.getDatanodeStorageReportMap(DatanodeReportType.ALL);
                            } catch (IOException e) {
                                LOG.error("Cannot get the datanodes from the RPC server", e);
                                return null;
                            }
                        }
                    });
            for (Entry<String, DatanodeStorageReport[]> entry : dnMap.entrySet()) {
                String nsId = entry.getKey();
                DatanodeStorageReport[] dns = entry.getValue();
                for (DatanodeStorageReport dn : dns) {
                    DatanodeInfo dnInfo = dn.getDatanodeInfo();
                    String ipAddr = dnInfo.getIpAddr();
                    ret.put(ipAddr, nsId);
                }
            }
        } catch (IOException e) {
            LOG.error("Cannot get Datanodes from the Namenodes: {}", e.getMessage());
        }
        return ret;
    }

    // 从 membershipStore.getNamenodeRegistrations(...) 获取 nn_ip -> ns
    private Map<String, String> getNamenodesSubcluster(MembershipStore membershipStore) {
        String localIp = "127.0.0.1";
        String localHostname = localIp;
        try {
            localHostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("Cannot get local host name");
        }

        Map<String, String> ret = new HashMap<>();
        try {
            GetNamenodeRegistrationsRequest request =
                    GetNamenodeRegistrationsRequest.newInstance();
            GetNamenodeRegistrationsResponse response = membershipStore.getNamenodeRegistrations(request);
            final List<MembershipState> nns = response.getNamenodeMemberships();
            for (MembershipState nn : nns) {
                try {
                    String nsId = nn.getNameserviceId();
                    String rpcAddress = nn.getRpcAddress();
                    String hostname = HostAndPort.fromString(rpcAddress).getHost();
                    ret.put(hostname, nsId);
                    if (hostname.equals(localHostname)) {
                        ret.put(localIp, nsId);
                    }

                    InetAddress addr = InetAddress.getByName(hostname);
                    String ipAddr = addr.getHostAddress();
                    ret.put(ipAddr, nsId);
                } catch (Exception e) {
                    LOG.error("Cannot get address for {}: {}", nn, e.getMessage());
                }
            }
        } catch (IOException ioe) {
            LOG.error("Cannot get Namenodes from the State Store", ioe);
        }
        return ret;
    }
}