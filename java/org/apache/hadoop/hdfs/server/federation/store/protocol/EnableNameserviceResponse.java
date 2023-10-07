package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for enabling a name service and updating its state in the
 * State Store.
 */
public abstract class EnableNameserviceResponse {

    public static EnableNameserviceResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(EnableNameserviceResponse.class);
    }

    public static EnableNameserviceResponse newInstance(boolean status)
            throws IOException {
        EnableNameserviceResponse response = newInstance();
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
