package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.DisabledNameservice;

import java.io.IOException;
import java.util.Set;

// 对 DisabledNameservice的管理（禁用 和 启用）
@InterfaceAudience.Private
@InterfaceStability.Evolving
public abstract class DisabledNameserviceStore
        extends CachedRecordStore<DisabledNameservice> {

    public DisabledNameserviceStore(StateStoreDriver driver) {
        super(DisabledNameservice.class, driver);
    }

    public abstract boolean disableNameservice(String nsId) throws IOException;

    public abstract boolean enableNameservice(String nsId) throws IOException;

    public abstract Set<String> getDisabledNameservices() throws IOException;
}
