package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for adding a mount table entry to the state store.
 */
public abstract class AddMountTableEntryResponse {

    public static AddMountTableEntryResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(AddMountTableEntryResponse.class);
    }

    @Public
    @Unstable
    public abstract boolean getStatus();

    @Public
    @Unstable
    public abstract void setStatus(boolean result);
}