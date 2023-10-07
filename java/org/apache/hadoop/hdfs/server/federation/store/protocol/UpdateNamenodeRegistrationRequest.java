package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeServiceState;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;

/**
 * API request for overriding an existing namenode registration in the state
 * store.
 */
public abstract class UpdateNamenodeRegistrationRequest {

    public static UpdateNamenodeRegistrationRequest newInstance()
            throws IOException {
        return StateStoreSerializer.newRecord(
                UpdateNamenodeRegistrationRequest.class);
    }

    public static UpdateNamenodeRegistrationRequest newInstance(
            String nameserviceId, String namenodeId,
            FederationNamenodeServiceState state) throws IOException {
        UpdateNamenodeRegistrationRequest request = newInstance();
        request.setNameserviceId(nameserviceId);
        request.setNamenodeId(namenodeId);
        request.setState(state);
        return request;
    }

    @Private
    @Unstable
    public abstract String getNameserviceId();

    @Private
    @Unstable
    public abstract String getNamenodeId();

    @Private
    @Unstable
    public abstract FederationNamenodeServiceState getState();

    @Private
    @Unstable
    public abstract void setNameserviceId(String nsId);

    @Private
    @Unstable
    public abstract void setNamenodeId(String nnId);

    @Private
    @Unstable
    public abstract void setState(FederationNamenodeServiceState state);
}