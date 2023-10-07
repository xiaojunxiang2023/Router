package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for the Router entering safe mode state and updating
 * its state in State Store.
 */
public abstract class EnterSafeModeRequest {
    public static EnterSafeModeRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(EnterSafeModeRequest.class);
    }
}