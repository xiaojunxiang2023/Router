package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for the Router leaving safe mode state and updating
 * its state in State Store.
 */
public abstract class LeaveSafeModeRequest {
    public static LeaveSafeModeRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(LeaveSafeModeRequest.class);
    }
}