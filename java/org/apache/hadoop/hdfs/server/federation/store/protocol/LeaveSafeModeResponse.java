package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for the Router leaving safe mode state and updating
 * its state in State Store.
 */
public abstract class LeaveSafeModeResponse {

    public static LeaveSafeModeResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(LeaveSafeModeResponse.class);
    }

    public static LeaveSafeModeResponse newInstance(boolean status)
            throws IOException {
        LeaveSafeModeResponse response = newInstance();
        response.setStatus(status);
        return response;
    }

    @Public
    @Unstable
    public abstract boolean getStatus();

    @Public
    @Unstable
    public abstract void setStatus(boolean result);
}