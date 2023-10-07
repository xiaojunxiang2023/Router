package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * API response for getting the destination subcluster of a file.
 */
public abstract class GetDestinationResponse {

    public static GetDestinationResponse newInstance()
            throws IOException {
        return StateStoreSerializer
                .newRecord(GetDestinationResponse.class);
    }

    public static GetDestinationResponse newInstance(
            Collection<String> nsIds) throws IOException {
        GetDestinationResponse request = newInstance();
        request.setDestinations(nsIds);
        return request;
    }

    @Public
    @Unstable
    public abstract Collection<String> getDestinations();

    @Public
    @Unstable
    public void setDestination(String nsId) {
        setDestinations(Collections.singletonList(nsId));
    }

    @Public
    @Unstable
    public abstract void setDestinations(Collection<String> nsIds);
}
