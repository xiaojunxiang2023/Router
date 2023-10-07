package org.apache.hadoop.hdfs.server.federation.store;

import org.apache.hadoop.hdfs.server.federation.store.records.BaseRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class StateStoreUtils {

    private static final Logger LOG =
            LoggerFactory.getLogger(StateStoreUtils.class);


    private StateStoreUtils() {
        // Utility class
    }

    // 根据 record获得父类类型 
    @SuppressWarnings("unchecked")
    public static <T extends BaseRecord>
    Class<? extends BaseRecord> getRecordClass(final Class<T> clazz) {

        // We ignore the Impl classes and go to the super class
        Class<? extends BaseRecord> actualClazz = clazz;
        while (actualClazz.getSimpleName().endsWith("Impl")) {
            actualClazz = (Class<? extends BaseRecord>) actualClazz.getSuperclass();
        }

        // Check if we went too far
        if (actualClazz.equals(BaseRecord.class)) {
            LOG.error("We went too far ({}) with {}", actualClazz, clazz);
            actualClazz = clazz;
        }
        return actualClazz;
    }

    // 同上，根据 record获得父类类型
    public static <T extends BaseRecord>
    Class<? extends BaseRecord> getRecordClass(final T record) {
        return getRecordClass(record.getClass());
    }

    // 根据 record获得父类的简名称
    public static <T extends BaseRecord> String getRecordName(
            final Class<T> clazz) {
        return getRecordClass(clazz).getSimpleName();
    }

    // 按规则 (Query)去 match过滤 records
    public static <T extends BaseRecord> List<T> filterMultiple(
            final Query<T> query, final Iterable<T> records) {

        List<T> matchingList = new ArrayList<>();
        for (T record : records) {
            if (query.matches(record)) {
                matchingList.add(record);
            }
        }
        return matchingList;
    }

    // 根据 address返回 host:port
    public static String getHostPortString(InetSocketAddress address) {
        if (null == address) {
            return "";
        }
        String hostName = address.getHostName();
        //如果 hostname是 0.0.0.0，马么就使用本机 hostname来代替
        if (hostName.equals("0.0.0.0")) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                LOG.error("Failed to get local host name", e);
                return "";
            }
        }
        return hostName + ":" + address.getPort();
    }

}
