package org.apache.hadoop.hdfs.server.federation.store.records;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

// 相比于 MembershipState,主要是对 NameNode元数据相关的信息(可用空间、文件数、可用 DataNode) 的描述
public abstract class MembershipStats extends BaseRecord {

    public static MembershipStats newInstance() {
        MembershipStats record =
                StateStoreSerializer.newRecord(MembershipStats.class);
        record.init();
        return record;
    }

    public abstract void setTotalSpace(long space);

    public abstract long getTotalSpace();

    public abstract void setAvailableSpace(long space);

    public abstract long getAvailableSpace();

    public abstract void setProvidedSpace(long capacity);

    public abstract long getProvidedSpace();

    public abstract void setNumOfFiles(long files);

    public abstract long getNumOfFiles();

    public abstract void setNumOfBlocks(long blocks);

    public abstract long getNumOfBlocks();

    public abstract void setNumOfBlocksMissing(long blocks);

    public abstract long getNumOfBlocksMissing();

    public abstract void setNumOfBlocksPendingReplication(long blocks);

    public abstract long getNumOfBlocksPendingReplication();

    public abstract void setNumOfBlocksUnderReplicated(long blocks);

    public abstract long getNumOfBlocksUnderReplicated();

    public abstract void setNumOfBlocksPendingDeletion(long blocks);

    public abstract long getNumOfBlocksPendingDeletion();

    public abstract void setNumOfActiveDatanodes(int nodes);

    public abstract int getNumOfActiveDatanodes();

    public abstract void setNumOfDeadDatanodes(int nodes);

    public abstract int getNumOfDeadDatanodes();

    public abstract void setNumOfStaleDatanodes(int nodes);

    public abstract int getNumOfStaleDatanodes();

    public abstract void setNumOfDecommissioningDatanodes(int nodes);

    public abstract int getNumOfDecommissioningDatanodes();

    public abstract void setNumOfDecomActiveDatanodes(int nodes);

    public abstract int getNumOfDecomActiveDatanodes();

    public abstract void setNumOfDecomDeadDatanodes(int nodes);

    public abstract int getNumOfDecomDeadDatanodes();

    public abstract void setNumOfInMaintenanceLiveDataNodes(int nodes);

    public abstract int getNumOfInMaintenanceLiveDataNodes();

    public abstract void setNumOfInMaintenanceDeadDataNodes(int nodes);

    public abstract int getNumOfInMaintenanceDeadDataNodes();

    public abstract void setNumOfEnteringMaintenanceDataNodes(int nodes);

    public abstract int getNumOfEnteringMaintenanceDataNodes();

    @Override
    public SortedMap<String, String> getPrimaryKeys() {
        return new TreeMap<>();
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
}
