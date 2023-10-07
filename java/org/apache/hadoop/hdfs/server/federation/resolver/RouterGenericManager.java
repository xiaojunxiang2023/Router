package org.apache.hadoop.hdfs.server.federation.resolver;

import java.io.IOException;

// 刷新 superUserGroup的配置
public interface RouterGenericManager {
    
    boolean refreshSuperUserGroupsConfiguration() throws IOException;
    
}
