package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;

import java.io.IOException;

/**
 * API request for listing namenode registrations present in the state store.
 */
public abstract class GetNamenodeRegistrationsRequest {

    public static GetNamenodeRegistrationsRequest newInstance()
            throws IOException {
        return StateStoreSerializer.newRecord(
                GetNamenodeRegistrationsRequest.class);
    }

    public static GetNamenodeRegistrationsRequest newInstance(
            MembershipState member) throws IOException {
        GetNamenodeRegistrationsRequest request = newInstance();
        request.setPartialMembership(member);
        return request;
    }

    @Public
    @Unstable
    public abstract MembershipState getPartialMembership();

    @Public
    @Unstable
    public abstract void setPartialMembership(MembershipState member);
}