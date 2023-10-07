package org.apache.hadoop.hdfs.server.federation.store.records;

import org.apache.hadoop.thirdparty.com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.util.Time;

import java.util.Map;

// 一条 Record数据
// 包含：
//  1、由 Record数据组成的主键
//  2、修改时间
//  3、创建时间

public abstract class BaseRecord implements Comparable<BaseRecord> {
    public static final String ERROR_MSG_CREATION_TIME_NEGATIVE =
            "The creation time for the record cannot be negative.";
    public static final String ERROR_MSG_MODIFICATION_TIME_NEGATIVE =
            "The modification time for the record cannot be negative.";

    public abstract void setDateModified(long time);

    public abstract long getDateModified();

    public abstract void setDateCreated(long time);

    public abstract long getDateCreated();

    // 获取到过期时间
    public abstract long getExpirationMs();

    public boolean isExpired() {
        return false;
    }

    // 将要删除的时间
    public long getDeletionMs() {
        return -1;
    }

    // 获得 Map映射，主键 -> Record值，主键可以是联合主键
    public abstract Map<String, String> getPrimaryKeys();

    // 初始化时间
    public void init() {
        initDefaultTimes();
    }

    private void initDefaultTimes() {
        long now = Time.now();
        this.setDateCreated(now);
        this.setDateModified(now);
    }

    // 连接主键成 联合组件
    public String getPrimaryKey() {
        return generateMashupKey(getPrimaryKeys());
    }

    @VisibleForTesting
    public boolean hasOtherFields() {
        return true;
    }

    // 从多个 value生成其联合主键
    protected static String generateMashupKey(final Map<String, String> keys) {
        StringBuilder builder = new StringBuilder();
        for (Object value : keys.values()) {
            if (builder.length() > 0) {
                builder.append("-");
            }
            builder.append(value);
        }
        return builder.toString();
    }

    // 即相当于 equals比较
    public boolean like(BaseRecord other) {
        if (other == null) {
            return false;
        }
        Map<String, String> thisKeys = this.getPrimaryKeys();
        Map<String, String> otherKeys = other.getPrimaryKeys();
        if (thisKeys == null) {
            return otherKeys == null;
        }
        return thisKeys.equals(otherKeys);
    }

    // 重写 equals 和 hashcode
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseRecord)) {
            return false;
        }

        BaseRecord baseObject = (BaseRecord) obj;
        Map<String, String> keyset1 = this.getPrimaryKeys();
        Map<String, String> keyset2 = baseObject.getPrimaryKeys();
        return keyset1.equals(keyset2);
    }

    @Override
    public int hashCode() {
        Map<String, String> keyset = this.getPrimaryKeys();
        return keyset.hashCode();
    }

    @Override
    public int compareTo(BaseRecord record) {
        if (record == null) {
            return -1;
        }
        return (int) (record.getDateModified() - this.getDateModified());
    }


    public boolean checkExpired(long currentTime) {
        long expiration = getExpirationMs();
        long modifiedTime = getDateModified();
        if (modifiedTime > 0 && expiration > 0) {
            return (modifiedTime + expiration) < currentTime;
        }
        return false;
    }

    // 根据是否过期 来返回是否应该 delete
    public boolean shouldBeDeleted(long currentTime) {
        long deletionTime = getDeletionMs();
        if (isExpired() && deletionTime > 0) {
            long elapsedTime = currentTime - (getDateModified() + getExpirationMs());
            return elapsedTime > deletionTime;
        } else {
            return false;
        }
    }

    public void validate() {
        if (getDateCreated() <= 0) {
            throw new IllegalArgumentException(ERROR_MSG_CREATION_TIME_NEGATIVE);
        } else if (getDateModified() <= 0) {
            throw new IllegalArgumentException(ERROR_MSG_MODIFICATION_TIME_NEGATIVE);
        }
    }

    @Override
    public String toString() {
        return getPrimaryKey();
    }
}
