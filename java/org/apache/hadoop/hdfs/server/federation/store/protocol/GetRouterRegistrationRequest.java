package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

/**
 * API request for retrieving a single router registration present in the state
 * store.
 */
public abstract class GetRouterRegistrationRequest {

    public static GetRouterRegistrationRequest newInstance() {
        return StateStoreSerializer.newRecord(GetRouterRegistrationRequest.class);
    }

    public static GetRouterRegistrationRequest newInstance(String routerId) {
        GetRouterRegistrationRequest request = newInstance();
        request.setRouterId(routerId);
        return request;
    }

    @Public
    @Unstable
    public abstract String getRouterId();

    @Public
    @Unstable
    public abstract void setRouterId(String routerId);
}