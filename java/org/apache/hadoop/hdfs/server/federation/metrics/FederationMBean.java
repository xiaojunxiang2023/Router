package org.apache.hadoop.hdfs.server.federation.metrics;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

import java.math.BigInteger;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface FederationMBean {
    String getNamenodes();

    String getNameservices();

    String getMountTable();

    String getRouters();

    long getTotalCapacity();

    long getUsedCapacity();

    long getRemainingCapacity();

    BigInteger getTotalCapacityBigInt();

    BigInteger getUsedCapacityBigInt();

    BigInteger getRemainingCapacityBigInt();

    long getProvidedSpace();

    int getNumNameservices();

    int getNumNamenodes();

    int getNumExpiredNamenodes();

    int getNumLiveNodes();

    int getNumDeadNodes();

    int getNumStaleNodes();

    int getNumDecommissioningNodes();

    int getNumDecomLiveNodes();

    int getNumDecomDeadNodes();

    int getNumInMaintenanceLiveDataNodes();

    int getNumInMaintenanceDeadDataNodes();

    int getNumEnteringMaintenanceDataNodes();

    String getNodeUsage();

    long getNumBlocks();

    long getNumOfMissingBlocks();

    long getNumOfBlocksPendingReplication();

    long getNumOfBlocksUnderReplicated();

    long getNumOfBlocksPendingDeletion();

    long getNumFiles();

    @Deprecated
    String getRouterStarted();

    @Deprecated
    String getVersion();

    @Deprecated
    String getCompiledDate();

    @Deprecated
    String getCompileInfo();

    @Deprecated
    String getHostAndPort();

    @Deprecated
    String getRouterId();

    String getClusterId();

    @Deprecated
    String getBlockPoolId();

    @Deprecated
    String getRouterStatus();

    @Deprecated
    long getCurrentTokensCount();

    @Deprecated
    boolean isSecurityEnabled();
}
