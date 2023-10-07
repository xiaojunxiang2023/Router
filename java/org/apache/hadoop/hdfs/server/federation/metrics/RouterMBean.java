package org.apache.hadoop.hdfs.server.federation.metrics;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface RouterMBean {
    String getRouterStarted();

    String getVersion();

    String getCompiledDate();

    String getCompileInfo();

    String getHostAndPort();

    String getRouterId();

    String getRouterStatus();

    String getClusterId();

    String getBlockPoolId();

    long getCurrentTokensCount();

    String getSafemode();

    boolean isSecurityEnabled();
}
