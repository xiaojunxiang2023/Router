package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

/**
 * API request for getting the disabled name services.
 */
public abstract class GetDisabledNameservicesRequest {

    public static GetDisabledNameservicesRequest newInstance() {
        return StateStoreSerializer.newRecord(GetDisabledNameservicesRequest.class);
    }
}
