package org.apache.hadoop.hdfs.server.federation.store.driver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;

// 序列化器
// 序列化，反序列化，创建一个 Record实例
public abstract class StateStoreSerializer {

    private static StateStoreSerializer defaultSerializer;

    public static StateStoreSerializer getSerializer() {
        return getSerializer(null);
    }

    public static StateStoreSerializer getSerializer(Configuration conf) {
        if (conf == null) {
            synchronized (StateStoreSerializer.class) {
                if (defaultSerializer == null) {
                    conf = new Configuration();
                    defaultSerializer = newSerializer(conf);
                }
            }
            return defaultSerializer;
        } else {
            return newSerializer(conf);
        }
    }

    private static StateStoreSerializer newSerializer(final Configuration conf) {
        Class<? extends StateStoreSerializer> serializerName = conf.getClass(
                RBFConfigKeys.FEDERATION_STORE_SERIALIZER_CLASS,
                RBFConfigKeys.FEDERATION_STORE_SERIALIZER_CLASS_DEFAULT,
                StateStoreSerializer.class);
        return ReflectionUtils.newInstance(serializerName, conf);
    }

    public static <T> T newRecord(Class<T> clazz) {
        return getSerializer(null).newRecordInstance(clazz);
    }

    public abstract <T> T newRecordInstance(Class<T> clazz);

    public abstract byte[] serialize(BaseRecord record);

    public abstract String serializeString(BaseRecord record);

    public abstract <T extends BaseRecord> T deserialize(
            byte[] byteArray, Class<T> clazz) throws IOException;

    public abstract <T extends BaseRecord> T deserialize(
            String data, Class<T> clazz) throws IOException;
}
