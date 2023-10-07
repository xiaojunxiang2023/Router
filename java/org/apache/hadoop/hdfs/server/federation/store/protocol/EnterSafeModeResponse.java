package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for the Router entering safe mode state and updating
 * its state in State Store.
 */
public abstract class EnterSafeModeResponse {

    public static EnterSafeModeResponse newInstance() throws IOException {
        return StateStoreSerializer.newRecord(EnterSafeModeResponse.class);
    }

    public static EnterSafeModeResponse newInstance(boolean status)
            throws IOException {
        EnterSafeModeResponse response = newInstance();
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