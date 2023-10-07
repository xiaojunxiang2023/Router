package org.apache.hadoop.hdfs.server.federation.router;

// Router服务的状态
public enum RouterServiceState {
    UNINITIALIZED,
    
    INITIALIZING,
    SAFEMODE,
    RUNNING,
    
    STOPPING,
    SHUTDOWN,
    EXPIRED;
}
