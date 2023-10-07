package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;

import java.io.IOException;

/**
 * API response for retrieving a single router registration present in the state
 * store.
 */
public abstract class GetRouterRegistrationResponse {

    public static GetRouterRegistrationResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(GetRouterRegistrationResponse.class);
    }

    @Public
    @Unstable
    public abstract RouterState getRouter() throws IOException;

    @Public
    @Unstable
    public abstract void setRouter(RouterState router) throws IOException;
}