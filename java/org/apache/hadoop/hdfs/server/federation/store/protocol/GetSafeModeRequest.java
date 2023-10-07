package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for verifying if current Router state is safe mode.
 */
public abstract class GetSafeModeRequest {
    public static GetSafeModeRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(GetSafeModeRequest.class);
    }
}