package org.apache.hadoop.hdfs.server.federation.store.impl;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.RouterStore;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.Query;
import org.apache.hadoop.hdfs.server.federation.store.records.QueryResult;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of the {@link RouterStore} state store API.
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class RouterStoreImpl extends RouterStore {

    public RouterStoreImpl(StateStoreDriver driver) {
        super(driver);
    }

    @Override
    public GetRouterRegistrationResponse getRouterRegistration(
            GetRouterRegistrationRequest request) throws IOException {

        final RouterState partial = RouterState.newInstance();
        partial.setAddress(request.getRouterId());
        final Query<RouterState> query = new Query<RouterState>(partial);
        RouterState record = getDriver().get(getRecordClass(), query);
        if (record != null) {
            overrideExpiredRecord(record);
        }
        GetRouterRegistrationResponse response =
                GetRouterRegistrationResponse.newInstance();
        response.setRouter(record);
        return response;
    }

    @Override
    public GetRouterRegistrationsResponse getRouterRegistrations(
            GetRouterRegistrationsRequest request) throws IOException {

        // Get all values from the cache
        QueryResult<RouterState> recordsAndTimeStamp =
                getCachedRecordsAndTimeStamp();
        List<RouterState> records = recordsAndTimeStamp.getRecords();
        long timestamp = recordsAndTimeStamp.getTimestamp();

        // Generate response
        GetRouterRegistrationsResponse response =
                GetRouterRegistrationsResponse.newInstance();
        response.setRouters(records);
        response.setTimestamp(timestamp);
        return response;
    }

    // 对 ZNode进行写数据, 更新 Router基本信息  [包含 (MembershipStore + MountTableStore) 的最后一次更新时间，还包含 adminAddress]
    @Override
    public RouterHeartbeatResponse routerHeartbeat(RouterHeartbeatRequest request)
            throws IOException {
        RouterState record = request.getRouter();
        boolean status = getDriver().put(record, true, false);
        return RouterHeartbeatResponse.newInstance(status);
    }
}
