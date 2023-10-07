package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for registering a router with the state store.
 */
public abstract class RouterHeartbeatResponse {

    public static RouterHeartbeatResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(RouterHeartbeatResponse.class);
    }

    public static RouterHeartbeatResponse newInstance(boolean status)
            throws IOException {
        RouterHeartbeatResponse response = newInstance();
        response.setStatus(status);
        return response;
    }

    @Public
    @Unstable
    public abstract boolean getStatus();

    @Public
    @Unstable
    public abstract void setStatus(boolean result);
}