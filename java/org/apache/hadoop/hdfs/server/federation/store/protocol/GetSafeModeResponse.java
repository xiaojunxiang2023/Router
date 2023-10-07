package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for verifying if current Router state is safe mode.
 */
public abstract class GetSafeModeResponse {

    public static GetSafeModeResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(GetSafeModeResponse.class);
    }

    public static GetSafeModeResponse newInstance(boolean isInSafeMode)
            throws IOException {
        GetSafeModeResponse response = newInstance();
        response.setSafeMode(isInSafeMode);
        return response;
    }

    @Public
    @Unstable
    public abstract boolean isInSafeMode();

    @Public
    @Unstable
    public abstract void setSafeMode(boolean isInSafeMode);
}