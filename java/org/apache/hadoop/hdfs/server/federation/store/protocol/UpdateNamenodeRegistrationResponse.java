package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API response for overriding an existing namenode registration in the state
 * store.
 */
public abstract class UpdateNamenodeRegistrationResponse {

    public static UpdateNamenodeRegistrationResponse newInstance() {
        return StateStoreSerializer.newRecord(
                UpdateNamenodeRegistrationResponse.class);
    }

    public static UpdateNamenodeRegistrationResponse newInstance(boolean status)
            throws IOException {
        UpdateNamenodeRegistrationResponse response = newInstance();
        response.setResult(status);
        return response;
    }

    @Private
    @Unstable
    public abstract boolean getResult();

    @Private
    @Unstable
    public abstract void setResult(boolean result);
}