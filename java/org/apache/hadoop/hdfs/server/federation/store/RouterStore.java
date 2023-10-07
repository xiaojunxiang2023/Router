package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;

import java.io.IOException;

// 对 RouterState(Router 相关的信息) 的管理
@InterfaceAudience.Private
@InterfaceStability.Evolving
public abstract class RouterStore extends CachedRecordStore<RouterState> {

    public RouterStore(StateStoreDriver driver) {
        super(RouterState.class, driver, true);
    }

    // 获取当前 Router
    public abstract GetRouterRegistrationResponse getRouterRegistration(
            GetRouterRegistrationRequest request) throws IOException;

    // 获取所有 Router
    public abstract GetRouterRegistrationsResponse getRouterRegistrations(
            GetRouterRegistrationsRequest request) throws IOException;

    // 心跳更新 Router基本信息  [包含 (MembershipStore + MountTableStore) 的最后一次更新时间，还包含 adminAddress]
    public abstract RouterHeartbeatResponse routerHeartbeat(
            RouterHeartbeatRequest request) throws IOException;
}
