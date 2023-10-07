package org.apache.hadoop.hdfs.server.federation.store.records;

import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.util.SortedMap;
import java.util.TreeMap;

public abstract class DisabledNameservice extends BaseRecord {

    public DisabledNameservice() {
        super();
    }

    // 创建一个实例 
    public static DisabledNameservice newInstance() {
        DisabledNameservice record =
                StateStoreSerializer.newRecord(DisabledNameservice.class);
        record.init();
        return record;
    }

    // 创建一个实例，并赋值
    public static DisabledNameservice newInstance(String nsId) {
        DisabledNameservice record = newInstance();
        record.setNameserviceId(nsId);
        return record;
    }

    public abstract String getNameserviceId();

    public abstract void setNameserviceId(String nameServiceId);

    @Override
    public SortedMap<String, String> getPrimaryKeys() {
        SortedMap<String, String> keyMap = new TreeMap<>();
        keyMap.put("nameServiceId", this.getNameserviceId());
        return keyMap;
    }

    @Override
    public boolean hasOtherFields() {
        // 除了主键，我们没有其他字段
        return false;
    }

    @Override
    public long getExpirationMs() {
        return -1;
    }
}
