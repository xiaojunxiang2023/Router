package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.ha.HAServiceProtocol.HAServiceState;

public enum FederationNamenodeServiceState {
    ACTIVE,
    OBSERVER,
    STANDBY,
    
    UNAVAILABLE,
    EXPIRED,
    DISABLED;

    // 根据 HAServiceState得到 FederationNamenodeServiceState
    public static FederationNamenodeServiceState getState(HAServiceState state) {
        switch (state) {
            case ACTIVE:
                return FederationNamenodeServiceState.ACTIVE;
            case OBSERVER:
                return FederationNamenodeServiceState.OBSERVER;
            case STANDBY:
                return FederationNamenodeServiceState.STANDBY;
                
            case INITIALIZING:
                return FederationNamenodeServiceState.UNAVAILABLE;
            case STOPPING:
                return FederationNamenodeServiceState.UNAVAILABLE;
            default:
                return FederationNamenodeServiceState.UNAVAILABLE;
        }
    }
}
