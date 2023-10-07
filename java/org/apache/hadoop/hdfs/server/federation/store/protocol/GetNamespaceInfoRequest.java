package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

/**
 * API response for listing HDFS namespaces present in the state store.
 */
public abstract class GetNamespaceInfoRequest {

    public static GetNamespaceInfoRequest newInstance() {
        return StateStoreSerializer.newRecord(GetNamespaceInfoRequest.class);
    }
}
