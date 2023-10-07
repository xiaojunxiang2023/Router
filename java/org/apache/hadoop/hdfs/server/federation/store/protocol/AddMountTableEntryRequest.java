package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;

/**
 * API request for adding a mount table entry to the state store.
 */
public abstract class AddMountTableEntryRequest {

    public static AddMountTableEntryRequest newInstance() {
        return StateStoreSerializer.newRecord(AddMountTableEntryRequest.class);
    }

    public static AddMountTableEntryRequest newInstance(MountTable newEntry) {
        AddMountTableEntryRequest request = newInstance();
        request.setEntry(newEntry);
        return request;
    }

    @Public
    @Unstable
    public abstract MountTable getEntry();

    @Public
    @Unstable
    public abstract void setEntry(MountTable mount);
}