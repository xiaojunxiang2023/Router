package org.apache.hadoop.hdfs.server.federation.resolver;

public interface FederationNamenodeContext {
    String getRpcAddress();
    String getServiceAddress();
    String getLifelineAddress();
    String getWebAddress();

    String getNameserviceId();
    String getNamenodeId();
    String getNamenodeKey();

    String getWebScheme();
    FederationNamenodeServiceState getState();
    long getDateModified();
}
