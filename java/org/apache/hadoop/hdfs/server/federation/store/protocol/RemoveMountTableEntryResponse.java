package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for removing a mount table path present in the state store.
 */
public abstract class RemoveMountTableEntryResponse {

    public static RemoveMountTableEntryResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(RemoveMountTableEntryResponse.class);
    }

    @Public
    @Unstable
    public abstract boolean getStatus();

    @Public
    @Unstable
    public abstract void setStatus(boolean result);
}