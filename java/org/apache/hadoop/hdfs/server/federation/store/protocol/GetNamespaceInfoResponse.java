package org.apache.hadoop.hdfs.server.federation.store.protocol;

import org.apache.hadoop.classification.InterfaceAudience.Public;
import org.apache.hadoop.classification.InterfaceStability.Unstable;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamespaceInfo;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;
import java.util.Set;

/**
 * API response for listing HDFS namespaces present in the state store.
 */
public abstract class GetNamespaceInfoResponse {

    public static GetNamespaceInfoResponse newInstance() {
        return StateStoreSerializer.newRecord(GetNamespaceInfoResponse.class);
    }

    public static GetNamespaceInfoResponse newInstance(
            Set<FederationNamespaceInfo> namespaces) throws IOException {
        GetNamespaceInfoResponse response = newInstance();
        response.setNamespaceInfo(namespaces);
        return response;
    }

    @Public
    @Unstable
    public abstract Set<FederationNamespaceInfo> getNamespaceInfo();

    @Public
    @Unstable
    public abstract void setNamespaceInfo(
            Set<FederationNamespaceInfo> namespaceInfo);
}