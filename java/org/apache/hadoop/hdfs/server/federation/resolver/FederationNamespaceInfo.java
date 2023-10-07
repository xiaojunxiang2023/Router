package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hadoop.hdfs.server.federation.router.RemoteLocationContext;

// 描述某个 ns的信息
public class FederationNamespaceInfo extends RemoteLocationContext {

    private final String blockPoolId;
    private final String clusterId;
    private final String nameserviceId;

    public FederationNamespaceInfo(String bpId, String clId, String nsId) {
        this.blockPoolId = bpId;
        this.clusterId = clId;
        this.nameserviceId = nsId;
    }

    @Override
    public String getNameserviceId() {
        return this.nameserviceId;
    }

    @Override
    public String getDest() {
        return this.nameserviceId;
    }

    @Override
    public String getSrc() {
        return null;
    }

    public String getClusterId() {
        return this.clusterId;
    }

    public String getBlockPoolId() {
        return this.blockPoolId;
    }

    @Override
    public String toString() {
        return this.nameserviceId + "->" + this.blockPoolId + ":" + this.clusterId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        FederationNamespaceInfo other = (FederationNamespaceInfo) obj;
        return new EqualsBuilder()
                .append(nameserviceId, other.nameserviceId)
                .append(clusterId, other.clusterId)
                .append(blockPoolId, other.blockPoolId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(nameserviceId)
                .append(clusterId)
                .append(blockPoolId)
                .toHashCode();
    }

    @Override
    public int compareTo(RemoteLocationContext info) {
        if (info instanceof FederationNamespaceInfo) {
            FederationNamespaceInfo other = (FederationNamespaceInfo) info;
            return new CompareToBuilder()
                    .append(nameserviceId, other.nameserviceId)
                    .append(clusterId, other.clusterId)
                    .append(blockPoolId, other.blockPoolId)
                    .toComparison();
        }
        return super.compareTo(info);
    }
}
