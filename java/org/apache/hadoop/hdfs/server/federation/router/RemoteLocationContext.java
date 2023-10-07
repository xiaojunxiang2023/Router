package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.commons.lang3.builder.HashCodeBuilder;

// 对应一个 ns，   src路径、ns_id://dest路径   
public abstract class RemoteLocationContext
        implements Comparable<RemoteLocationContext> {

    public abstract String getNameserviceId();

    public abstract String getDest();

    public abstract String getSrc();

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(getNameserviceId())
                .append(getDest())
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RemoteLocationContext) {
            RemoteLocationContext other = (RemoteLocationContext) obj;
            return this.getNameserviceId().equals(other.getNameserviceId()) &&
                    this.getDest().equals(other.getDest());
        }
        return false;
    }

    @Override
    public int compareTo(RemoteLocationContext info) {
        int ret = this.getNameserviceId().compareTo(info.getNameserviceId());
        if (ret == 0) {
            ret = this.getDest().compareTo(info.getDest());
        }
        return ret;
    }
}
