package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;

import java.io.IOException;
import java.util.List;

/**
 * API response for listing mount table entries present in the state store.
 */
public abstract class GetMountTableEntriesResponse {

    public static GetMountTableEntriesResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(GetMountTableEntriesResponse.class);
    }

    @Public
    @Unstable
    public abstract List<MountTable> getEntries() throws IOException;

    @Public
    @Unstable
    public abstract void setEntries(List<MountTable> entries)
            throws IOException;

    @Public
    @Unstable
    public abstract long getTimestamp();

    @Public
    @Unstable
    public abstract void setTimestamp(long time);
}