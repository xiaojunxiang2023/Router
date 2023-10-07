package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for removing a mount table path present in the state store.
 */
public abstract class RemoveMountTableEntryRequest {

    public static RemoveMountTableEntryRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(RemoveMountTableEntryRequest.class);
    }

    public static RemoveMountTableEntryRequest newInstance(String path)
            throws IOException {
        RemoveMountTableEntryRequest request = newInstance();
        request.setSrcPath(path);
        return request;
    }

    @Public
    @Unstable
    public abstract String getSrcPath();

    @Public
    @Unstable
    public abstract void setSrcPath(String path);
}