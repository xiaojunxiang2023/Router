package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for disabling a name service and updating its state in the
 * State Store.
 */
public abstract class DisableNameserviceResponse {

    public static DisableNameserviceResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(DisableNameserviceResponse.class);
    }

    public static DisableNameserviceResponse newInstance(boolean status)
            throws IOException {
        DisableNameserviceResponse response = newInstance();
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
