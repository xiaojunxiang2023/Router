package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;

import java.io.IOException;
import java.util.List;

/**
 * API response for listing namenode registrations present in the state store.
 */
public abstract class GetNamenodeRegistrationsResponse {

    public static GetNamenodeRegistrationsResponse newInstance()
            throws IOException {
        return StateStoreSerializer.newRecord(
                GetNamenodeRegistrationsResponse.class);
    }

    public static GetNamenodeRegistrationsResponse newInstance(
            List<MembershipState> records) throws IOException {
        GetNamenodeRegistrationsResponse response = newInstance();
        response.setNamenodeMemberships(records);
        return response;
    }

    @Public
    @Unstable
    public abstract List<MembershipState> getNamenodeMemberships() throws IOException;

    @Public
    @Unstable
    public abstract void setNamenodeMemberships(
            List<MembershipState> records) throws IOException;
}