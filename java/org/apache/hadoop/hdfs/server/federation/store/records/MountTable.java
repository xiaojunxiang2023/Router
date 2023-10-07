package org.apache.hadoop.hdfs.server.federation.store.records;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.server.federation.resolver.RemoteLocation;
import org.apache.hadoop.hdfs.server.federation.resolver.order.DestinationOrder;
import org.apache.hadoop.hdfs.server.federation.router.RouterPermissionChecker;
import org.apache.hadoop.hdfs.server.federation.router.RouterQuotaUsage;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public abstract class MountTable extends BaseRecord {

    public static final String ERROR_MSG_NO_SOURCE_PATH =
            "Invalid entry, no source path specified ";
    public static final String ERROR_MSG_MUST_START_WITH_BACK_SLASH =
            "Invalid entry, all mount points must start with / ";
    public static final String ERROR_MSG_NO_DEST_PATH_SPECIFIED =
            "Invalid entry, no destination paths specified ";
    public static final String ERROR_MSG_INVAILD_DEST_NS =
            "Invalid entry, invalid destination nameservice ";
    public static final String ERROR_MSG_INVAILD_DEST_PATH =
            "Invalid entry, invalid destination path ";
    public static final String ERROR_MSG_ALL_DEST_MUST_START_WITH_BACK_SLASH =
            "Invalid entry, all destination must start with / ";
    private static final String ERROR_MSG_FAULT_TOLERANT_MULTI_DEST =
            "Invalid entry, fault tolerance requires multiple destinations ";
    private static final String ERROR_MSG_FAULT_TOLERANT_ALL =
            "Invalid entry, fault tolerance only supported for ALL order ";

    // 路径比较器
    public static final Comparator<String> PATH_COMPARATOR =
            (o1, o2) -> {
                String s1 = o1.replace('/', ' ');
                String s2 = o2.replace('/', ' ');
                return s1.compareTo(s2);
            };
    public static final Comparator<MountTable> SOURCE_COMPARATOR =
            (m1, m2) -> {
                String src1 = m1.getSourcePath();
                String src2 = m2.getSourcePath();
                return PATH_COMPARATOR.compare(src1, src2);
            };


    public MountTable() {
        super();
    }

    // 创建一个实例
    public static MountTable newInstance() {
        MountTable record = StateStoreSerializer.newRecord(MountTable.class);
        record.init();
        return record;
    }

    // 创建一个实例，并赋值
    public static MountTable newInstance(final String src,
                                         final Map<String, String> destinations,
                                         long dateCreated, long dateModified) throws IOException {

        MountTable record = newInstance(src, destinations);
        record.setDateCreated(dateCreated);
        record.setDateModified(dateModified);
        return record;
    }

    /**
     * Constructor for a mount table entry with multiple destinations.
     *
     * @param src Source path in the mount entry.
     * @param destinations Name service destinations of the mount point.
     * @throws IOException If it cannot be created.
     */
    public static MountTable newInstance(final String src,
                                         final Map<String, String> destinations) throws IOException {
        MountTable record = newInstance();

        // Normalize the mount path
        record.setSourcePath(normalizeFileSystemPath(src));

        // Build a list of remote locations
        final List<RemoteLocation> locations = new LinkedList<>();
        for (Entry<String, String> entry : destinations.entrySet()) {
            String nsId = entry.getKey();
            String path = normalizeFileSystemPath(entry.getValue());
            RemoteLocation location = new RemoteLocation(nsId, path, src);
            locations.add(location);
        }

        // Set the serialized dest string
        record.setDestinations(locations);

        // Set permission fields
        UserGroupInformation ugi = NameNode.getRemoteUser();
        record.setOwnerName(ugi.getShortUserName());
        String group = ugi.getGroups().isEmpty() ? ugi.getShortUserName()
                : ugi.getPrimaryGroupName();
        record.setGroupName(group);
        record.setMode(new FsPermission(
                RouterPermissionChecker.MOUNT_TABLE_PERMISSION_DEFAULT));

        // Set quota for mount table
        RouterQuotaUsage quota = new RouterQuotaUsage.Builder()
                .fileAndDirectoryCount(RouterQuotaUsage.QUOTA_USAGE_COUNT_DEFAULT)
                .quota(HdfsConstants.QUOTA_RESET)
                .spaceConsumed(RouterQuotaUsage.QUOTA_USAGE_COUNT_DEFAULT)
                .spaceQuota(HdfsConstants.QUOTA_RESET).build();
        record.setQuota(quota);

        // Validate
        record.validate();
        return record;
    }

    // 源路径
    public abstract String getSourcePath();

    public abstract void setSourcePath(String path);

    // Destinations
    public abstract List<RemoteLocation> getDestinations();

    public abstract void setDestinations(List<RemoteLocation> dests);

    // 获取默认 Destination，即索引0
    public RemoteLocation getDefaultLocation() {
        List<RemoteLocation> dests = this.getDestinations();
        if (dests == null || dests.isEmpty()) {
            return null;
        }
        return dests.get(0);
    }

    // 添加一个 Destination
    public abstract boolean addDestination(String nsId, String path);


    public abstract boolean isReadOnly();

    public abstract void setReadOnly(boolean ro);

    // DestinationOrder
    public abstract DestinationOrder getDestOrder();

    public abstract void setDestOrder(DestinationOrder order);

    // 当目标 Destination失败时, 是否可以容错降级到其他的 Destination
    public abstract boolean isFaultTolerant();

    public abstract void setFaultTolerant(boolean faultTolerant);

    public abstract String getOwnerName();

    public abstract void setOwnerName(String owner);

    public abstract String getGroupName();

    public abstract void setGroupName(String group);

    public abstract FsPermission getMode();

    public abstract void setMode(FsPermission mode);

    public abstract RouterQuotaUsage getQuota();

    public abstract void setQuota(RouterQuotaUsage quota);


    // 获得一个 Map
    @Override
    public SortedMap<String, String> getPrimaryKeys() {
        SortedMap<String, String> map = new TreeMap<>();
        map.put("sourcePath", this.getSourcePath());
        return map;
    }

    // 是否相似（equals）
    @Override
    public boolean like(final BaseRecord o) {
        if (o instanceof MountTable) {
            MountTable other = (MountTable) o;
            if (getSourcePath() != null &&
                    !getSourcePath().equals(other.getSourcePath())) {
                return false;
            }
            if (getDestinations() != null &&
                    !getDestinations().equals(other.getDestinations())) {
                return false;
            }
            return true;
        }
        return false;
    }

    // 是否属于 FOLDER_ALL的子集
    public boolean isAll() {
        DestinationOrder order = getDestOrder();
        return DestinationOrder.FOLDER_ALL.contains(order);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getSourcePath());
        sb.append("->");
        List<RemoteLocation> destinations = this.getDestinations();
        sb.append(destinations);
        if (destinations != null && destinations.size() > 1) {
            sb.append("[").append(this.getDestOrder()).append("]");
        }
        if (this.isReadOnly()) {
            sb.append("[RO]");
        }
        if (this.isFaultTolerant()) {
            sb.append("[FT]");
        }

        if (this.getOwnerName() != null) {
            sb.append("[owner:").append(this.getOwnerName()).append("]");
        }

        if (this.getGroupName() != null) {
            sb.append("[group:").append(this.getGroupName()).append("]");
        }

        if (this.getMode() != null) {
            sb.append("[mode:").append(this.getMode()).append("]");
        }

        if (this.getQuota() != null) {
            sb.append("[quota:").append(this.getQuota()).append("]");
        }

        return sb.toString();
    }

    @Override
    public void validate() {
        super.validate();
        if (this.getSourcePath() == null || this.getSourcePath().length() == 0) {
            throw new IllegalArgumentException(
                    ERROR_MSG_NO_SOURCE_PATH + this);
        }
        if (!this.getSourcePath().startsWith("/")) {
            throw new IllegalArgumentException(
                    ERROR_MSG_MUST_START_WITH_BACK_SLASH + this);
        }
        if (this.getDestinations() == null || this.getDestinations().size() == 0) {
            throw new IllegalArgumentException(
                    ERROR_MSG_NO_DEST_PATH_SPECIFIED + this);
        }
        for (RemoteLocation loc : getDestinations()) {
            String nsId = loc.getNameserviceId();
            if (nsId == null || nsId.length() == 0) {
                throw new IllegalArgumentException(
                        ERROR_MSG_INVAILD_DEST_NS + this);
            }
            if (loc.getDest() == null || loc.getDest().length() == 0) {
                throw new IllegalArgumentException(
                        ERROR_MSG_INVAILD_DEST_PATH + this);
            }
            if (!loc.getDest().startsWith("/")) {
                throw new IllegalArgumentException(
                        ERROR_MSG_ALL_DEST_MUST_START_WITH_BACK_SLASH + this);
            }
        }
        if (isFaultTolerant()) {
            // 如果设置了支持容错的话，
            // 必须得 getDestinations().size() >= 2 && 负载均衡策略属于(SPACE, RANDOM, HASH_ALL)
            if (getDestinations().size() < 2) {
                throw new IllegalArgumentException(
                        ERROR_MSG_FAULT_TOLERANT_MULTI_DEST + this);
            }
            if (!isAll()) {
                throw new IllegalArgumentException(
                        ERROR_MSG_FAULT_TOLERANT_ALL + this);
            }
        }
    }

    @Override
    public long getExpirationMs() {
        return 0;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(this.getSourcePath())
                .append(this.getDestinations())
                .append(this.isReadOnly())
                .append(this.getDestOrder())
                .append(this.isFaultTolerant())
                .append(this.getQuota().getQuota())
                .append(this.getQuota().getSpaceQuota())
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MountTable) {
            MountTable other = (MountTable) obj;
            return new EqualsBuilder()
                    .append(this.getSourcePath(), other.getSourcePath())
                    .append(this.getDestinations(), other.getDestinations())
                    .append(this.isReadOnly(), other.isReadOnly())
                    .append(this.getDestOrder(), other.getDestOrder())
                    .append(this.isFaultTolerant(), other.isFaultTolerant())
                    .append(this.getQuota().getQuota(), other.getQuota().getQuota())
                    .append(this.getQuota().getSpaceQuota(),
                            other.getQuota().getSpaceQuota())
                    .isEquals();
        }
        return false;
    }

    // 给 path 封装一下
    private static String normalizeFileSystemPath(final String path) {
        Path normalizedPath = new Path(path);
        return normalizedPath.toString();
    }
}
