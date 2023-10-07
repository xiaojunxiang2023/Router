package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;

import java.io.IOException;

// 对 MembershipState(NameNode 相关的信息) 的管理
@InterfaceAudience.Private
@InterfaceStability.Evolving
public abstract class MembershipStore
        extends CachedRecordStore<MembershipState> {

    protected MembershipStore(StateStoreDriver driver) {
        super(MembershipState.class, driver, true);
    }

    // upsert ZNode中 NameNode的一条 MemberShipState数据
    public abstract NamenodeHeartbeatResponse namenodeHeartbeat(
            NamenodeHeartbeatRequest request) throws IOException;

    // 获得所有 NameNode的信息
    public abstract GetNamenodeRegistrationsResponse getNamenodeRegistrations(
            GetNamenodeRegistrationsRequest request) throws IOException;

    // 获得缓存过期的 NameNode的信息
    public abstract GetNamenodeRegistrationsResponse
    getExpiredNamenodeRegistrations(GetNamenodeRegistrationsRequest request)
            throws IOException;

    // 获得所有 namespace的信息
    public abstract GetNamespaceInfoResponse getNamespaceInfo(
            GetNamespaceInfoRequest request) throws IOException;

    // 更新 NameNode的信息
    public abstract UpdateNamenodeRegistrationResponse updateNamenodeRegistration(
            UpdateNamenodeRegistrationRequest request) throws IOException;
}
