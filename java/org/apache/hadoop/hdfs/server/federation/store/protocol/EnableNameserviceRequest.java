package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

/**
 * API request for enabling a name service and updating its state in the
 * State Store.
 */
public abstract class EnableNameserviceRequest {

    public static EnableNameserviceRequest newInstance() {
        return StateStoreSerializer.newRecord(EnableNameserviceRequest.class);
    }

    public static EnableNameserviceRequest newInstance(String nsId) {
        EnableNameserviceRequest request = newInstance();
        request.setNameServiceId(nsId);
        return request;
    }

    @Public
    @Unstable
    public abstract String getNameServiceId();

    @Public
    @Unstable
    public abstract void setNameServiceId(String nsId);
}
