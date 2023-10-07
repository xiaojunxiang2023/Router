package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for registering a namenode with the state store.
 */
public abstract class NamenodeHeartbeatResponse {

    public static NamenodeHeartbeatResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(NamenodeHeartbeatResponse.class);
    }

    public static NamenodeHeartbeatResponse newInstance(boolean status)
            throws IOException {
        NamenodeHeartbeatResponse response = newInstance();
        response.setResult(status);
        return response;
    }

    @Private
    @Unstable
    public abstract boolean getResult();

    @Private
    @Unstable
    public abstract void setResult(boolean result);
}