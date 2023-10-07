package org.apache.hadoop.hdfs.server.federation.store.records;

import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeContext;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeServiceState;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeServiceState.*;

// 相比于 MembershipStats, 主要是 NameNode服务相关的信息 的描述
public abstract class MembershipState extends BaseRecord
        implements FederationNamenodeContext {
    public static final String ERROR_MSG_NO_NS_SPECIFIED =
            "Invalid registration, no nameservice specified ";
    public static final String ERROR_MSG_NO_WEB_ADDR_SPECIFIED =
            "Invalid registration, no web address specified ";
    public static final String ERROR_MSG_NO_RPC_ADDR_SPECIFIED =
            "Invalid registration, no rpc address specified ";
    public static final String ERROR_MSG_NO_BP_SPECIFIED =
            "Invalid registration, no block pool specified ";

    private static long expirationMs;

    // 待删除时间
    private static long deletionMs;

    public static final Comparator<MembershipState> NAME_COMPARATOR = MembershipState::compareNameTo;


    public MembershipState() {
        super();
    }

    // 创建一个 Record实例
    public static MembershipState newInstance() {
        MembershipState record =
                StateStoreSerializer.newRecord(MembershipState.class);
        record.init();
        return record;
    }

    // 创建一个 Record实例，并赋值
    public static MembershipState newInstance(String router, String nameservice,
                                              String namenode, String clusterId, String blockPoolId, String rpcAddress,
                                              String serviceAddress, String lifelineAddress,
                                              String webScheme, String webAddress,
                                              FederationNamenodeServiceState state, boolean safemode) {

        MembershipState record = MembershipState.newInstance();
        record.setRouterId(router);
        record.setNameserviceId(nameservice);
        record.setNamenodeId(namenode);
        record.setRpcAddress(rpcAddress);
        record.setServiceAddress(serviceAddress);
        record.setLifelineAddress(lifelineAddress);
        record.setWebAddress(webAddress);
        record.setIsSafeMode(safemode);
        record.setState(state);
        record.setClusterId(clusterId);
        record.setBlockPoolId(blockPoolId);
        record.setWebScheme(webScheme);
        record.validate();
        return record;
    }

    public abstract void setRouterId(String routerId);

    public abstract String getRouterId();

    public abstract void setNameserviceId(String nameserviceId);

    public abstract void setNamenodeId(String namenodeId);

    public abstract void setWebAddress(String webAddress);

    public abstract void setRpcAddress(String rpcAddress);

    public abstract void setServiceAddress(String serviceAddress);

    public abstract void setLifelineAddress(String lifelineAddress);

    public abstract void setIsSafeMode(boolean isSafeMode);

    public abstract void setClusterId(String clusterId);

    public abstract void setBlockPoolId(String blockPoolId);

    public abstract void setState(FederationNamenodeServiceState state);

    public abstract void setWebScheme(String webScheme);

    public abstract String getNameserviceId();

    public abstract String getNamenodeId();

    public abstract String getClusterId();

    public abstract String getBlockPoolId();

    public abstract String getRpcAddress();

    public abstract String getServiceAddress();

    public abstract String getLifelineAddress();

    public abstract String getWebAddress();

    public abstract boolean getIsSafeMode();

    public abstract String getWebScheme();

    public abstract FederationNamenodeServiceState getState();

    public abstract void setStats(MembershipStats stats);

    public abstract MembershipStats getStats();

    public abstract void setLastContact(long contact);

    public abstract long getLastContact();

    // 是否相似 (equals)
    @Override
    public boolean like(BaseRecord o) {
        if (o instanceof MembershipState) {
            MembershipState other = (MembershipState) o;
            if (getRouterId() != null &&
                    !getRouterId().equals(other.getRouterId())) {
                return false;
            }
            if (getNameserviceId() != null &&
                    !getNameserviceId().equals(other.getNameserviceId())) {
                return false;
            }
            if (getNamenodeId() != null &&
                    !getNamenodeId().equals(other.getNamenodeId())) {
                return false;
            }
            if (getRpcAddress() != null &&
                    !getRpcAddress().equals(other.getRpcAddress())) {
                return false;
            }
            if (getClusterId() != null &&
                    !getClusterId().equals(other.getClusterId())) {
                return false;
            }
            if (getBlockPoolId() != null &&
                    !getBlockPoolId().equals(other.getBlockPoolId())) {
                return false;
            }
            return getState() == null ||
                    getState().equals(other.getState());
        }
        return false;
    }

    @Override
    public String toString() {
        return getRouterId() + "->" + getNameserviceId() + ":" + getNamenodeId()
                + ":" + getRpcAddress() + "-" + getState();
    }

    // 获得一个 Map
    @Override
    public SortedMap<String, String> getPrimaryKeys() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("routerId", getRouterId());
        map.put("nameserviceId", getNameserviceId());
        map.put("namenodeId", getNamenodeId());
        return map;
    }

    public boolean isAvailable() {
        return getState() == ACTIVE;
    }


    @Override
    public void validate() {
        super.validate();
        if (getNameserviceId() == null || getNameserviceId().length() == 0) {
            throw new IllegalArgumentException(
                    ERROR_MSG_NO_NS_SPECIFIED + this);
        }
        if (getWebAddress() == null || getWebAddress().length() == 0) {
            throw new IllegalArgumentException(
                    ERROR_MSG_NO_WEB_ADDR_SPECIFIED + this);
        }
        if (getRpcAddress() == null || getRpcAddress().length() == 0) {
            throw new IllegalArgumentException(
                    ERROR_MSG_NO_RPC_ADDR_SPECIFIED + this);
        }
        if (!isBadState() &&
                (getBlockPoolId().isEmpty() || getBlockPoolId().length() == 0)) {
            throw new IllegalArgumentException(
                    ERROR_MSG_NO_BP_SPECIFIED + this);
        }
    }


    public void overrideState(FederationNamenodeServiceState newState) {
        this.setState(newState);
    }

    // 按照 nameservice、namenode、router 进行比较
    public int compareNameTo(MembershipState other) {
        int ret = this.getNameserviceId().compareTo(other.getNameserviceId());
        if (ret == 0) {
            ret = this.getNamenodeId().compareTo(other.getNamenodeId());
        }
        if (ret == 0) {
            ret = this.getRouterId().compareTo(other.getRouterId());
        }
        return ret;
    }

    public String getNamenodeKey() {
        return getNamenodeKey(this.getNameserviceId(), this.getNamenodeId());
    }

    public static String getNamenodeKey(String nsId, String nnId) {
        return nsId + "-" + nnId;
    }

    private boolean isBadState() {
        return this.getState() == EXPIRED || this.getState() == UNAVAILABLE;
    }

    @Override
    public boolean checkExpired(long currentTime) {
        if (super.checkExpired(currentTime)) {
            this.setState(EXPIRED);
            return true;
        }
        return false;
    }

    @Override
    public long getExpirationMs() {
        return MembershipState.expirationMs;
    }

    public static void setExpirationMs(long time) {
        MembershipState.expirationMs = time;
    }

    @Override
    public boolean isExpired() {
        return getState() == EXPIRED;
    }

    @Override
    public long getDeletionMs() {
        return MembershipState.deletionMs;
    }

    public static void setDeletionMs(long time) {
        MembershipState.deletionMs = time;
    }
}
