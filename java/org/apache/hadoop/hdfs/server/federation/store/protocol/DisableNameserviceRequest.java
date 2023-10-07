package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

/**
 * API request for disabling a name service and updating its state in the
 * State Store.
 */
public abstract class DisableNameserviceRequest {

    public static DisableNameserviceRequest newInstance() {
        return StateStoreSerializer.newRecord(DisableNameserviceRequest.class);
    }

    public static DisableNameserviceRequest newInstance(String nsId) {
        DisableNameserviceRequest request = newInstance();
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
