package org.apache.hadoop.hdfs.server.federation.resolver.order;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.server.federation.resolver.PathLocation;

// 只使用子路径比父路径多一层 HashResolver
public class HashFirstResolver extends HashResolver {

    @Override
    public String getFirstNamespace(final String path, final PathLocation loc) {
        String srcPath = loc.getSourcePath();
        String trimmedPath = trimPathToChild(path, srcPath);
        LOG.debug("Only using the first part of the path: {} -> {}",
                path, trimmedPath);
        return super.getFirstNamespace(trimmedPath, loc);
    }

    // 返回父路径 + 第一层独特的子路径
    private static String trimPathToChild(String path, String parent) {
        // 不合法的，子路径长度短于父路径
        if (path.length() <= parent.length()) {
            return parent;
        }
        String remainder = path.substring(parent.length());
        String[] components = remainder.replaceFirst("^/", "").split(Path.SEPARATOR);
        if (components.length > 0 && components[0].length() > 0) {
            if (parent.endsWith(Path.SEPARATOR)) {
                return parent + components[0];
            } else {
                return parent + Path.SEPARATOR + components[0];
            }
        } else {
            return parent;
        }
    }
}