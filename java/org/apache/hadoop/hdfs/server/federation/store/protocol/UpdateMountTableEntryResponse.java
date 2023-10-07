package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for updating the destination of an existing mount point in the
 * state store.
 */
public abstract class UpdateMountTableEntryResponse {

    public static UpdateMountTableEntryResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(UpdateMountTableEntryResponse.class);
    }

    @Public
    @Unstable
    public abstract boolean getStatus();

    @Public
    @Unstable
    public abstract void setStatus(boolean result);
}