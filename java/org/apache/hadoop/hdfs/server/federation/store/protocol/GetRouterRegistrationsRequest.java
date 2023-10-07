package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for retrieving a all non-expired router registrations present in
 * the state store.
 */
public abstract class GetRouterRegistrationsRequest {

    public static GetRouterRegistrationsRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(GetRouterRegistrationsRequest.class);
    }

}