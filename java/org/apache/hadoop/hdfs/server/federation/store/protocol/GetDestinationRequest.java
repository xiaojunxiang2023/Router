package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for getting the destination subcluster of a file.
 */
public abstract class GetDestinationRequest {

    public static GetDestinationRequest newInstance()
            throws IOException {
        return StateStoreSerializer
                .newRecord(GetDestinationRequest.class);
    }

    public static GetDestinationRequest newInstance(String srcPath)
            throws IOException {
        GetDestinationRequest request = newInstance();
        request.setSrcPath(srcPath);
        return request;
    }

    public static GetDestinationRequest newInstance(Path srcPath)
            throws IOException {
        return newInstance(srcPath.toString());
    }

    @Public
    @Unstable
    public abstract String getSrcPath();

    @Public
    @Unstable
    public abstract void setSrcPath(String srcPath);
}
