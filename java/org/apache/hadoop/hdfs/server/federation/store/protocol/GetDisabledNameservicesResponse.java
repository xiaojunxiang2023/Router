package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.util.Set;

/**
 * API response for getting the disabled nameservices in the state store.
 */
public abstract class GetDisabledNameservicesResponse {

    public static GetDisabledNameservicesResponse newInstance() {
        return StateStoreSerializer.newRecord(
                GetDisabledNameservicesResponse.class);
    }

    public static GetDisabledNameservicesResponse newInstance(
            Set<String> nsIds) {
        GetDisabledNameservicesResponse response = newInstance();
        response.setNameservices(nsIds);
        return response;
    }

    @Public
    @Unstable
    public abstract Set<String> getNameservices();

    @Public
    @Unstable
    public abstract void setNameservices(Set<String> nameservices);

}
