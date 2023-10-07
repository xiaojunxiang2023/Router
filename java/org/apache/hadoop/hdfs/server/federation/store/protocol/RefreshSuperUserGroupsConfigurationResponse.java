package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for refreshing super user groups on router.
 */
public abstract class RefreshSuperUserGroupsConfigurationResponse {

    public static RefreshSuperUserGroupsConfigurationResponse newInstance()
            throws IOException {
        return StateStoreSerializer.
                newRecord(RefreshSuperUserGroupsConfigurationResponse.class);
    }

    public static RefreshSuperUserGroupsConfigurationResponse
    newInstance(boolean status) throws IOException {
        RefreshSuperUserGroupsConfigurationResponse response = newInstance();
        response.setStatus(status);
        return response;
    }

    @Public
    @Unstable
    public abstract boolean getStatus();

    @Public
    @Unstable
    public abstract void setStatus(boolean result);
}