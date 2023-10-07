package org.apache.hadoop.hdfs.server.federation.metrics;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface FederationRPCMBean {
    long getProxyOps();

    double getProxyAvg();

    long getProcessingOps();

    double getProcessingAvg();

    long getProxyOpFailureCommunicate();

    long getProxyOpFailureStandby();

    long getProxyOpFailureClientOverloaded();

    long getProxyOpNotImplemented();

    long getProxyOpRetries();

    long getProxyOpNoNamenodes();

    long getRouterFailureStateStoreOps();

    long getRouterFailureReadOnlyOps();

    long getRouterFailureLockedOps();

    long getRouterFailureSafemodeOps();

    int getRpcServerCallQueue();

    int getRpcServerNumOpenConnections();

    int getRpcClientNumConnections();

    int getRpcClientNumActiveConnections();

    int getRpcClientNumCreatingConnections();

    int getRpcClientNumConnectionPools();

    String getRpcClientConnections();

    String getAsyncCallerPool();
}
