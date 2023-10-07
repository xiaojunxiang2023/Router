package org.apache.hadoop.hdfs.server.federation.store.records;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.util.SortedMap;
import java.util.TreeMap;

// MembershipStore 和 MountTableStore 的版本信息，版本即最后一次更新的时间戳
public abstract class StateStoreVersion extends BaseRecord {

    public static StateStoreVersion newInstance() {
        return StateStoreSerializer.newRecord(StateStoreVersion.class);
    }

    public static StateStoreVersion newInstance(long membershipVersion,
                                                long mountTableVersion) {
        StateStoreVersion record = newInstance();
        record.setMembershipVersion(membershipVersion);
        record.setMountTableVersion(mountTableVersion);
        return record;
    }

    public abstract long getMembershipVersion();

    public abstract void setMembershipVersion(long version);

    public abstract long getMountTableVersion();

    public abstract void setMountTableVersion(long version);

    @Override
    public SortedMap<String, String> getPrimaryKeys() {
        // 此 Record不直接存储，不需要键
        SortedMap<String, String> map = new TreeMap<>();
        return map;
    }

    @Override
    public long getExpirationMs() {
        return -1;
    }

    @Override
    public void setDateModified(long time) {
    }

    @Override
    public long getDateModified() {
        return 0;
    }

    @Override
    public void setDateCreated(long time) {
    }

    @Override
    public long getDateCreated() {
        return 0;
    }

    @Override
    public String toString() {
        return "Membership: " + getMembershipVersion() +
                " Mount Table: " + getMountTableVersion();
    }
}
