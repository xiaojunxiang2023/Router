package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for listing mount table entries present in the state store.
 */
public abstract class GetMountTableEntriesRequest {

    public static GetMountTableEntriesRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(GetMountTableEntriesRequest.class);
    }

    public static GetMountTableEntriesRequest newInstance(String srcPath)
            throws IOException {
        GetMountTableEntriesRequest request = newInstance();
        request.setSrcPath(srcPath);
        return request;
    }

    @Public
    @Unstable
    public abstract String getSrcPath();

    @Public
    @Unstable
    public abstract void setSrcPath(String path);
}