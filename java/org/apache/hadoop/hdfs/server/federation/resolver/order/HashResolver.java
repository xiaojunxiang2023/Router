package org.apache.hadoop.hdfs.server.federation.resolver.order;

import org.apache.hadoop.hdfs.server.federation.resolver.PathLocation;
import org.apache.hadoop.hdfs.server.federation.utils.ConsistentHashRing;
import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashResolver implements OrderedResolver {

    protected static final Logger LOG = LoggerFactory.getLogger(HashResolver.class);


    // hash(ns_id_set) -> 哈希环
    private final Map<Integer, ConsistentHashRing> hashResolverMap;

    // 临时文件的正则表达式, "或" 连接
    private static final Pattern TEMP_FILE_PATTERN = Pattern.compile(StringUtils.join("|", new String[]{
            "(.+)\\.COPYING$",
            "(.+)\\._COPYING_.*$",
            "(.+)\\.tmp$",
            "_temp/(.+)$",
            "_temporary/(.+)\\." + "\\p{XDigit}" + "{8}-" +
                    "\\p{XDigit}" + "{4}-" + "\\p{XDigit}" + "{4}-" + "\\p{XDigit}" + "{4}-" +
                    "\\p{XDigit}" + "{12}" + "$",
            "(.*)_temporary/\\d/_temporary/" + "attempt_\\d+_\\d{4}_._\\d{6}_\\d{2}" + "/(.+)$"}));


    public HashResolver() {
        this.hashResolverMap = new ConcurrentHashMap<>();
    }

    // 根据 ns_id_set获取哈希环
    private ConsistentHashRing getHashResolver(final Set<String> namespaces) {
        final int hash = namespaces.hashCode();
        return this.hashResolverMap.computeIfAbsent(hash, k -> new ConsistentHashRing(namespaces));
    }


    // 核心方法实现, 获得最优的 ns
    // 即靠 哈希环.getLocation("文件路径{如果是临时文件的话,已对临时文件处理过}")
    @Override
    public String getFirstNamespace(final String path, final PathLocation loc) {
        String finalPath = extractTempFileName(path);
        Set<String> namespaces = loc.getNamespaces();
        ConsistentHashRing locator = getHashResolver(namespaces);
        String hashedSubcluster = locator.getLocation(finalPath);
        if (hashedSubcluster == null) {
            String srcPath = loc.getSourcePath();
            LOG.error("Cannot find subcluster for {} ({} -> {})",
                    srcPath, path, finalPath);
        }
        LOG.debug("Namespace for {} ({}) is {}", path, finalPath, hashedSubcluster);
        return hashedSubcluster;
    }

    // 获取处理后的值，例如 hosts._COPYING 返回 hosts
    @VisibleForTesting
    public static String extractTempFileName(final String input) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = TEMP_FILE_PATTERN.matcher(input);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                // matcher.group() 讲解: https://www.cnblogs.com/jiafuwei/p/6080984.html
                // 获得正则表达式框中的括号里的值
                String match = matcher.group(i);
                if (match != null) {
                    sb.append(match);
                }
            }
        }
        if (sb.length() > 0) {
            String ret = sb.toString();
            LOG.debug("Extracted {} from {}", ret, input);
            return ret;
        }
        return input;
    }
}