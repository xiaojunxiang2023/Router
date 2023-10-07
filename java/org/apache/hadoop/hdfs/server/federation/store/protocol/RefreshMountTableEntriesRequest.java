package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for refreshing mount table cached entries from state store.
 */
public abstract class RefreshMountTableEntriesRequest {

    public static RefreshMountTableEntriesRequest newInstance()
            throws IOException {
        return StateStoreSerializer
                .newRecord(RefreshMountTableEntriesRequest.class);
    }
}