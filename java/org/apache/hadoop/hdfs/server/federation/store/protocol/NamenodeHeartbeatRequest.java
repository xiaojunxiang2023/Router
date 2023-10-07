package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;

import java.io.IOException;

/**
 * API request for registering a namenode with the state store.
 */
public abstract class NamenodeHeartbeatRequest {

    public static NamenodeHeartbeatRequest newInstance() throws IOException {
        return StateStoreSerializer.newRecord(NamenodeHeartbeatRequest.class);
    }

    public static NamenodeHeartbeatRequest newInstance(MembershipState namenode)
            throws IOException {
        NamenodeHeartbeatRequest request = newInstance();
        request.setNamenodeMembership(namenode);
        return request;
    }

    @Private
    @Unstable
    public abstract MembershipState getNamenodeMembership()
            throws IOException;

    @Private
    @Unstable
    public abstract void setNamenodeMembership(MembershipState report)
            throws IOException;
}