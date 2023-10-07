package org.apache.hadoop.hdfs.server.federation.metrics;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface StateStoreMBean {
    long getReadOps();

    double getReadAvg();

    long getWriteOps();

    double getWriteAvg();

    long getFailureOps();

    double getFailureAvg();

    long getRemoveOps();

    double getRemoveAvg();
}
