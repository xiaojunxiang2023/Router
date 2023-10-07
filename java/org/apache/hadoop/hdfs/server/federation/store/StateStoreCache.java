package org.apache.hadoop.hdfs.server.federation.store;

import java.io.IOException;

public interface StateStoreCache {

    // 从 ZNode中加载数据到缓存，被 StateStoreCacheUpdateService线程定期调用

    // force参数：是否强制加载
    // 返回值：是否成功
    boolean loadCache(boolean force) throws IOException;
    
}
