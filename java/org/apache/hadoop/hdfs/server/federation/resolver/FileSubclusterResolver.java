package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

import java.io.IOException;
import java.util.List;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface FileSubclusterResolver {

    // 从缓存中获得 path对应的 PathLocation（远程 NameSpaces目标位置）
    PathLocation getDestinationForPath(String path) throws IOException;

    // 从缓存中获得 path孩子 的挂载点s
    List<String> getMountPoints(String path) throws IOException;

    String getDefaultNamespace();
}
