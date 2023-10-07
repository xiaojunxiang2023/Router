package org.apache.hadoop.hdfs.server.federation.store.impl;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.store.DisabledNameserviceStore;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.records.DisabledNameservice;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementation of {@link DisabledNameserviceStore}.
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class DisabledNameserviceStoreImpl extends DisabledNameserviceStore {

    public DisabledNameserviceStoreImpl(StateStoreDriver driver) {
        super(driver);
    }

    @Override
    public boolean disableNameservice(String nsId)
            throws IOException {

        DisabledNameservice record =
                DisabledNameservice.newInstance(nsId);
        return getDriver().put(record, false, false);
    }

    @Override
    public boolean enableNameservice(String nsId)
            throws IOException {

        DisabledNameservice record =
                DisabledNameservice.newInstance(nsId);
        return getDriver().remove(record);
    }

    @Override
    public Set<String> getDisabledNameservices() throws IOException {
        Set<String> disabledNameservices = new TreeSet<>();
        for (DisabledNameservice record : getCachedRecords()) {
            String nsId = record.getNameserviceId();
            disabledNameservices.add(nsId);
        }
        return disabledNameservices;
    }
}
