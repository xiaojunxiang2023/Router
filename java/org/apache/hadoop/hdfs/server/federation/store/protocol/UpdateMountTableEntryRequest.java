package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;

import java.io.IOException;

/**
 * API request for updating the destination of an existing mount point in the
 * state store.
 */
public abstract class UpdateMountTableEntryRequest {

    public static UpdateMountTableEntryRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(UpdateMountTableEntryRequest.class);
    }

    public static UpdateMountTableEntryRequest newInstance(MountTable entry)
            throws IOException {
        UpdateMountTableEntryRequest request = newInstance();
        request.setEntry(entry);
        return request;
    }

    @Public
    @Unstable
    public abstract MountTable getEntry() throws IOException;

    @Public
    @Unstable
    public abstract void setEntry(MountTable mount) throws IOException;
}