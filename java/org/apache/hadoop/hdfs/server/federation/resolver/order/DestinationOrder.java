package org.apache.hadoop.hdfs.server.federation.resolver.order;

import java.util.EnumSet;

// 负载均衡策略
public enum DestinationOrder {
    LOCAL,
    SPACE,
    RANDOM,
    HASH_ALL,
    HASH;

    public static final EnumSet<DestinationOrder> FOLDER_ALL = EnumSet.of(SPACE, RANDOM, HASH_ALL);
}