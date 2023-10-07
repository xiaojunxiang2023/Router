package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for refreshing mount table entries cache from state store.
 */
public abstract class RefreshMountTableEntriesResponse {

    public static RefreshMountTableEntriesResponse newInstance()
            throws IOException {
        return StateStoreSerializer
                .newRecord(RefreshMountTableEntriesResponse.class);
    }

    @Public
    @Unstable
    public abstract boolean getResult();

    @Public
    @Unstable
    public abstract void setResult(boolean result);
}