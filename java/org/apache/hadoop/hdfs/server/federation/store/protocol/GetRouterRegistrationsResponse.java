package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;

import java.io.IOException;
import java.util.List;

/**
 * API response for retrieving a all non-expired router registrations present in
 * the state store.
 */
public abstract class GetRouterRegistrationsResponse {

    public static GetRouterRegistrationsResponse newInstance()
            throws IOException {
        return StateStoreSerializer.newRecord(GetRouterRegistrationsResponse.class);
    }

    @Public
    @Unstable
    public abstract List<RouterState> getRouters() throws IOException;

    @Public
    @Unstable
    public abstract void setRouters(List<RouterState> routers)
            throws IOException;

    @Public
    @Unstable
    public abstract long getTimestamp();

    @Public
    @Unstable
    public abstract void setTimestamp(long time);
}