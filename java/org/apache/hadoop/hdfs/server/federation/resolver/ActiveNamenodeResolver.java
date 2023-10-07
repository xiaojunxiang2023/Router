package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface ActiveNamenodeResolver {

    // response.getNamenodeMemberships()获取的 nn数量为1时，将之更新为 ActiveNN
    void updateActiveNamenode(String ns, InetSocketAddress successfulAddress) throws IOException;

    // 根据 ns_id获得 namenode列表，按 ACTIVE、STANDBY、UNAVAILABLE优先级取排序
    List<? extends FederationNamenodeContext> getNamenodesForNameserviceId(String nameserviceId) throws IOException;

    // 同理，用的是 bp_id
    List<? extends FederationNamenodeContext> getNamenodesForBlockPoolId(String blockPoolId) throws IOException;

    // 发送心跳
    boolean registerNamenode(NamenodeStatusReport report) throws IOException;

    // 获得所有启用的 ns
    Set<FederationNamespaceInfo> getNamespaces() throws IOException;

    // 获得所有被禁用的 ns
    Set<String> getDisabledNamespaces() throws IOException;

    void setRouterId(String routerId);
}
