package org.apache.hadoop.hdfs.protocolPB;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.proto.RouterProtocolProtos;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenSelector;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.ipc.ProtocolInfo;
import org.apache.hadoop.security.KerberosInfo;
import org.apache.hadoop.security.token.TokenInfo;

/**
 * ? 为什么是和 NameNode 通信？Protocol that a clients use to communicate with the NameNode.
 * Note: This extends the protocolbuffer service based interface to
 * add annotations required for security.
 */
@InterfaceAudience.Private
@InterfaceStability.Stable
@TokenInfo(DelegationTokenSelector.class)
@KerberosInfo(
        serverPrincipal = RBFConfigKeys.DFS_ROUTER_KERBEROS_PRINCIPAL_KEY)
@ProtocolInfo(protocolName = HdfsConstants.ROUTER_ADMIN_PROTOCOL_NAME,
        protocolVersion = 1)
public interface RouterAdminProtocolPB extends
        RouterProtocolProtos.RouterAdminProtocolService.BlockingInterface {
}



