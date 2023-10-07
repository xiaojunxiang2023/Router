package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for refreshing super user groups on router.
 */
public abstract class RefreshSuperUserGroupsConfigurationRequest {
    public static RefreshSuperUserGroupsConfigurationRequest newInstance()
            throws IOException {
        return StateStoreSerializer
                .newRecord(RefreshSuperUserGroupsConfigurationRequest.class);
    }
}