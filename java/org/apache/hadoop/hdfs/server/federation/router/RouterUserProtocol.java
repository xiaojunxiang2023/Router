package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.server.federation.resolver.ActiveNamenodeResolver;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamespaceInfo;
import org.apache.hadoop.hdfs.server.namenode.NameNode.OperationCategory;
import org.apache.hadoop.security.Groups;
import org.apache.hadoop.security.RefreshUserMappingsProtocol;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authorize.ProxyUsers;
import org.apache.hadoop.tools.GetUserMappingsProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.apache.hadoop.hdfs.server.federation.router.RouterRpcServer.merge;

public class RouterUserProtocol
        implements RefreshUserMappingsProtocol, GetUserMappingsProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(RouterUserProtocol.class);

    private final RouterRpcServer rpcServer;
    private final RouterRpcClient rpcClient;
    private final ActiveNamenodeResolver namenodeResolver;

    public RouterUserProtocol(RouterRpcServer server) {
        this.rpcServer = server;
        this.rpcClient = this.rpcServer.getRPCClient();
        this.namenodeResolver = this.rpcServer.getNamenodeResolver();
    }
  
  /*
  
    if(namenodeResolver.getNamespaces().isEmpty){
        本地调用
    } else {
        rpcClient.远程调用
    }
  
   */

    @Override
    public void refreshUserToGroupsMappings() throws IOException {
        LOG.debug("Refresh user groups mapping in Router.");
        rpcServer.checkOperation(OperationCategory.UNCHECKED);
        Set<FederationNamespaceInfo> nss = namenodeResolver.getNamespaces();
        if (nss.isEmpty()) {
            Groups.getUserToGroupsMappingService().refresh();
        } else {
            RemoteMethod method = new RemoteMethod(RefreshUserMappingsProtocol.class, "refreshUserToGroupsMappings");
            rpcClient.invokeConcurrent(nss, method);
        }
    }

    @Override
    public void refreshSuperUserGroupsConfiguration() throws IOException {
        LOG.debug("Refresh superuser groups configuration in Router.");
        rpcServer.checkOperation(OperationCategory.UNCHECKED);
        Set<FederationNamespaceInfo> nss = namenodeResolver.getNamespaces();
        if (nss.isEmpty()) {
            ProxyUsers.refreshSuperUserGroupsConfiguration();
        } else {
            RemoteMethod method = new RemoteMethod(RefreshUserMappingsProtocol.class, "refreshSuperUserGroupsConfiguration");
            rpcClient.invokeConcurrent(nss, method);
        }
    }

    @Override
    public String[] getGroupsForUser(String user) throws IOException {
        LOG.debug("Getting groups for user {}", user);
        rpcServer.checkOperation(OperationCategory.UNCHECKED);
        Set<FederationNamespaceInfo> nss = namenodeResolver.getNamespaces();
        if (nss.isEmpty()) {
            return UserGroupInformation.createRemoteUser(user).getGroupNames();
        } else {
            RemoteMethod method = new RemoteMethod(GetUserMappingsProtocol.class, "getGroupsForUser", new Class<?>[]{String.class}, user);
            Map<FederationNamespaceInfo, String[]> results = rpcClient.invokeConcurrent(nss, method, String[].class);
            return merge(results, String.class);
        }
    }
}
