package org.apache.hadoop.hdfs.server.federation.resolver.order;

import org.apache.hadoop.hdfs.server.federation.resolver.PathLocation;


// 负载均衡选择器
public interface OrderedResolver {

    String getFirstNamespace(String path, PathLocation loc);
    
}