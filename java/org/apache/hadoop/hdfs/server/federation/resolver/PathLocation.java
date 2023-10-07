package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.hdfs.server.federation.resolver.order.DestinationOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PathLocation {

    private static final Logger LOG = LoggerFactory.getLogger(PathLocation.class);

    // src路径
    private final String sourcePath;
    // 多个远程路径
    private final List<RemoteLocation> destinations;

    // 负载均衡策略
    private final DestinationOrder destOrder;


    // 构造方法
    public PathLocation(
            String source, List<RemoteLocation> dest, DestinationOrder order) {
        this.sourcePath = source;
        this.destinations = Collections.unmodifiableList(dest);
        this.destOrder = order;
    }

    public PathLocation(String source, List<RemoteLocation> dest) {
        this(source, dest, DestinationOrder.HASH);
    }

    public PathLocation(final PathLocation other) {
        this.sourcePath = other.sourcePath;
        this.destinations = Collections.unmodifiableList(other.destinations);
        this.destOrder = other.destOrder;
    }

    // 将字段的 ns_id放在最前面
    public PathLocation(PathLocation other, String firstNsId) {
        this.sourcePath = other.sourcePath;
        this.destOrder = other.destOrder;
        this.destinations = orderedNamespaces(other.destinations, firstNsId);
    }

    private static List<RemoteLocation> orderedNamespaces(final List<RemoteLocation> original, final String nsId) {
        if (original.size() <= 1) {
            return original;
        }

        LinkedList<RemoteLocation> newDestinations = new LinkedList<>();
        boolean found = false;
        for (RemoteLocation dest : original) {
            if (dest.getNameserviceId().equals(nsId)) {
                found = true;
                newDestinations.addFirst(dest);
            } else {
                newDestinations.add(dest);
            }
        }

        if (!found) {
            LOG.debug("Cannot find location with namespace {} in {}",
                    nsId, original);
        }
        return Collections.unmodifiableList(newDestinations);
    }

    public String getSourcePath() {
        return this.sourcePath;
    }

    public Set<String> getNamespaces() {
        Set<String> namespaces = new HashSet<>();
        List<RemoteLocation> locations = this.getDestinations();
        for (RemoteLocation location : locations) {
            String nsId = location.getNameserviceId();
            namespaces.add(nsId);
        }
        return namespaces;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RemoteLocation destination : this.destinations) {
            String nsId = destination.getNameserviceId();
            String path = destination.getDest();
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(nsId).append("->").append(path);
        }
        if (this.destinations.size() > 1) {
            sb.append(" [")
                    .append(this.destOrder.toString())
                    .append("]");
        }
        return sb.toString();
    }

    public boolean hasMultipleDestinations() {
        return this.destinations.size() > 1;
    }

    public List<RemoteLocation> getDestinations() {
        return Collections.unmodifiableList(this.destinations);
    }

    public DestinationOrder getDestinationOrder() {
        return this.destOrder;
    }
    
    // 即 destinations[0]
    public RemoteLocation getDefaultLocation() {
        if (destinations.isEmpty() || destinations.get(0).getDest() == null) {
            throw new UnsupportedOperationException(
                    "Unsupported path " + sourcePath + " please check mount table");
        }
        return destinations.get(0);
    }
}