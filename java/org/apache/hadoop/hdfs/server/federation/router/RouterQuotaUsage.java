package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.fs.QuotaUsage;
import org.apache.hadoop.fs.StorageType;
import org.apache.hadoop.hdfs.protocol.DSQuotaExceededException;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.NSQuotaExceededException;
import org.apache.hadoop.hdfs.server.namenode.DirectoryWithQuotaFeature;
import org.apache.hadoop.hdfs.server.namenode.Quota;
import org.apache.hadoop.util.StringUtils;

/**
 * The subclass of {@link QuotaUsage} used in Router-based federation.
 */
public final class RouterQuotaUsage extends QuotaUsage {

    /** Default quota usage count. */
    public static final long QUOTA_USAGE_COUNT_DEFAULT = 0;

    private RouterQuotaUsage(Builder builder) {
        super(builder);
    }

    /** Build the instance based on the builder. */
    public static class Builder extends QuotaUsage.Builder {

        public RouterQuotaUsage build() {
            return new RouterQuotaUsage(this);
        }

        @Override
        public Builder fileAndDirectoryCount(long count) {
            super.fileAndDirectoryCount(count);
            return this;
        }

        @Override
        public Builder quota(long quota) {
            super.quota(quota);
            return this;
        }

        @Override
        public Builder spaceConsumed(long spaceConsumed) {
            super.spaceConsumed(spaceConsumed);
            return this;
        }

        @Override
        public Builder spaceQuota(long spaceQuota) {
            super.spaceQuota(spaceQuota);
            return this;
        }

        @Override
        public Builder typeConsumed(long[] typeConsumed) {
            super.typeConsumed(typeConsumed);
            return this;
        }

        @Override
        public Builder typeQuota(long[] typeQuota) {
            super.typeQuota(typeQuota);
            return this;
        }

        @Override
        public Builder typeQuota(StorageType type, long quota) {
            super.typeQuota(type, quota);
            return this;
        }
    }

    /**
     * Verify if namespace quota is violated once quota is set. Relevant
     * method {@link DirectoryWithQuotaFeature#verifyNamespaceQuota}.
     * @throws NSQuotaExceededException If the quota is exceeded.
     */
    public void verifyNamespaceQuota() throws NSQuotaExceededException {
        long quota = getQuota();
        long fileAndDirectoryCount = getFileAndDirectoryCount();
        if (Quota.isViolated(quota, fileAndDirectoryCount)) {
            throw new NSQuotaExceededException(quota, fileAndDirectoryCount);
        }
    }

    /**
     * Verify if storage space quota is violated once quota is set. Relevant
     * method {@link DirectoryWithQuotaFeature#verifyStoragespaceQuota}.
     * @throws DSQuotaExceededException If the quota is exceeded.
     */
    public void verifyStoragespaceQuota() throws DSQuotaExceededException {
        long spaceQuota = getSpaceQuota();
        long spaceConsumed = getSpaceConsumed();
        if (Quota.isViolated(spaceQuota, spaceConsumed)) {
            throw new DSQuotaExceededException(spaceQuota, spaceConsumed);
        }
    }

    /**
     * Verify space quota by storage type is violated once quota is set. Relevant
     * method {@link DirectoryWithQuotaFeature#verifyQuotaByStorageType}.
     * @throws DSQuotaExceededException If the quota is exceeded.
     */
    public void verifyQuotaByStorageType() throws DSQuotaExceededException {
        for (StorageType t : StorageType.getTypesSupportingQuota()) {
            long typeQuota = getTypeQuota(t);
            if (typeQuota == HdfsConstants.QUOTA_RESET) {
                continue;
            }
            long typeConsumed = getTypeConsumed(t);
            if (Quota.isViolated(typeQuota, typeConsumed)) {
                throw new DSQuotaExceededException(typeQuota, typeConsumed);
            }
        }
    }

    @Override
    public String toString() {
        String nsQuota = "-";
        String nsCount = "-";
        long quota = getQuota();
        if (quota != HdfsConstants.QUOTA_RESET) {
            nsQuota = String.valueOf(quota);
            nsCount = String.valueOf(getFileAndDirectoryCount());
        }

        String ssQuota = "-";
        String ssCount = "-";
        long spaceQuota = getSpaceQuota();
        if (spaceQuota != HdfsConstants.QUOTA_RESET) {
            ssQuota = StringUtils.byteDesc(spaceQuota);
            ssCount = StringUtils.byteDesc(getSpaceConsumed());
        }

        return "[NsQuota: " + nsQuota + "/" +
                nsCount +
                ", SsQuota: " + ssQuota +
                "/" + ssCount +
                "]";
    }
}
