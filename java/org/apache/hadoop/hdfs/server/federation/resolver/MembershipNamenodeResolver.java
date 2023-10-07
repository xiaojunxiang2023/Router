package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.hdfs.server.federation.store.*;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipStats;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeServiceState.*;

public class MembershipNamenodeResolver implements ActiveNamenodeResolver, StateStoreCache {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipNamenodeResolver.class);

    private final StateStoreService stateStore;
    private MembershipStore membershipInterface;
    private DisabledNameserviceStore disabledNameserviceInterface;

    private String routerId;

    // 缓存：ns_id -> namenode详细服务信息
    private final Map<String, List<? extends FederationNamenodeContext>> cacheNS;
    // 缓存：ns_id -> bp信息
    private final Map<String, List<? extends FederationNamenodeContext>> cacheBP;


    public MembershipNamenodeResolver(StateStoreService store) {
        this.stateStore = store;
        this.cacheNS = new ConcurrentHashMap<>();
        this.cacheBP = new ConcurrentHashMap<>();
        if (this.stateStore != null) {
            // 订阅外部缓存
            this.stateStore.registerCacheExternal(this);
        }
    }

    @Override
    public boolean loadCache(boolean force) {
        try {
            // 先更新 MembershipStore
            MembershipStore membership = getMembershipStore();
            membership.loadCache(force);
            DisabledNameserviceStore disabled = getDisabledNameserviceStore();
            // 再更新 DisabledNameserviceStore
            disabled.loadCache(force);
        } catch (IOException e) {
            LOG.error("Cannot update membership from the State Store", e);
        }

        // 再清空自己的缓存
        cacheBP.clear();
        cacheNS.clear();
        return true;
    }

    private synchronized MembershipStore getMembershipStore() throws IOException {
        if (this.membershipInterface == null) {
            this.membershipInterface = getStoreInterface(MembershipStore.class);
        }
        return this.membershipInterface;
    }

    private synchronized DisabledNameserviceStore getDisabledNameserviceStore()
            throws IOException {
        if (this.disabledNameserviceInterface == null) {
            this.disabledNameserviceInterface = getStoreInterface(DisabledNameserviceStore.class);
        }
        return this.disabledNameserviceInterface;
    }

    private <T extends RecordStore<?>> T getStoreInterface(Class<T> clazz)
            throws IOException {
        T store = this.stateStore.getRegisteredRecordStore(clazz);
        if (store == null) {
            throw new IOException("State Store does not have an interface for " + clazz.getSimpleName());
        }
        return store;
    }


    @Override
    public void updateActiveNamenode(final String nsId, final InetSocketAddress address) throws IOException {
        try {
            MembershipState partial = MembershipState.newInstance();
            String rpcAddress = address.getHostName() + ":" + address.getPort();
            partial.setRpcAddress(rpcAddress);
            partial.setNameserviceId(nsId);

            GetNamenodeRegistrationsRequest request = GetNamenodeRegistrationsRequest.newInstance(partial);

            MembershipStore membership = getMembershipStore();
            GetNamenodeRegistrationsResponse response = membership.getNamenodeRegistrations(request);
            List<MembershipState> records = response.getNamenodeMemberships();

            if (records != null && records.size() == 1) {
                MembershipState record = records.get(0);
                UpdateNamenodeRegistrationRequest updateRequest = UpdateNamenodeRegistrationRequest.newInstance(
                        record.getNameserviceId(), record.getNamenodeId(), ACTIVE);
                membership.updateNamenodeRegistration(updateRequest);

                cacheNS.remove(nsId);
                // 直接 clear，因为根据 ns_id去获取对应的 bp_id的开销很大
                cacheBP.clear();
            }
        } catch (StateStoreUnavailableException e) {
            LOG.error("Cannot update {} as active, State Store unavailable", address);
        }
    }

    @Override
    public List<? extends FederationNamenodeContext> getNamenodesForNameserviceId(final String nsId) throws IOException {

        List<? extends FederationNamenodeContext> ret = cacheNS.get(nsId);
        // 缓存里有值的话，直接返回
        if (ret != null) {
            return ret;
        }

        final List<MembershipState> result;
        try {
            MembershipState partial = MembershipState.newInstance();
            partial.setNameserviceId(nsId);
            GetNamenodeRegistrationsRequest request = GetNamenodeRegistrationsRequest.newInstance(partial);
            result = getRecentRegistrationForQuery(request);
        } catch (StateStoreUnavailableException e) {
            LOG.error("Cannot get active NN for {}, State Store unavailable", nsId);
            return null;
        }
        if (result.isEmpty()) {
            LOG.error("Cannot locate eligible NNs for {}", nsId);
            return null;
        }

        // 标记 disable信息
        try {
            Set<String> disabled = getDisabledNameserviceStore().getDisabledNameservices();
            if (disabled == null) {
                LOG.error("Cannot get disabled name services");
            } else {
                for (MembershipState nn : result) {
                    if (disabled.contains(nn.getNameserviceId())) {
                        nn.setState(FederationNamenodeServiceState.DISABLED);
                    }
                }
            }
        } catch (StateStoreUnavailableException e) {
            LOG.error("Cannot get disabled name services, State Store unavailable");
        }

        ret = Collections.unmodifiableList(result);
        // 这是自己新造出来的，放到缓存里保留【ns_id，namenode排序列表】
        cacheNS.put(nsId, result);
        return ret;
    }


    @Override
    public List<? extends FederationNamenodeContext> getNamenodesForBlockPoolId(final String bpId) throws IOException {

        List<? extends FederationNamenodeContext> ret = cacheBP.get(bpId);
        if (ret == null) {
            try {
                // 缓存里没值，则自己造
                MembershipState partial = MembershipState.newInstance();
                partial.setBlockPoolId(bpId);
                GetNamenodeRegistrationsRequest request = GetNamenodeRegistrationsRequest.newInstance(partial);

                final List<MembershipState> result = getRecentRegistrationForQuery(request);
                if (result.isEmpty()) {
                    LOG.error("Cannot locate eligible NNs for {}", bpId);
                } else {
                    // 这是自己新造出来的，放到缓存里保留【bp_id，namenode排序列表】
                    cacheBP.put(bpId, result);
                    ret = result;
                }
            } catch (StateStoreUnavailableException e) {
                LOG.error("Cannot get active NN for {}, State Store unavailable", bpId);
                return null;
            }
        }
        if (ret == null) {
            return null;
        }
        return Collections.unmodifiableList(ret);
    }

    // 获得最近更新的 namenode列表, 列表按状态优先级排序
    private List<MembershipState> getRecentRegistrationForQuery(GetNamenodeRegistrationsRequest request) throws IOException {

        MembershipStore membershipStore = getMembershipStore();
        // 获取所有的 namenode，即使是同一份namenode 但在不同的 router中，要返回多份
        GetNamenodeRegistrationsResponse response = membershipStore.getNamenodeRegistrations(request);

        List<MembershipState> memberships = response.getNamenodeMemberships();
        Iterator<MembershipState> iterator = memberships.iterator();
        while (iterator.hasNext()) {
            MembershipState membership = iterator.next();
            if (membership.getState() == EXPIRED) {
                iterator.remove();
            } else {
                membership.getState();
            }
        }

        List<MembershipState> priorityList = new ArrayList<>(memberships);
        priorityList.sort(new NamenodePriorityComparator());

        LOG.debug("Selected most recent NN {} for query", priorityList);
        return priorityList;
    }

    // 发送 NameNode心跳, 
    // 靠 getMembershipStore().namenodeHeartbeat(request) 底层调用 upsert ZNode
    @Override
    public boolean registerNamenode(NamenodeStatusReport report) throws IOException {

        if (this.routerId == null) {
            LOG.warn("Cannot register namenode, router ID is not known {}", report);
            return false;
        }

        MembershipState record = MembershipState.newInstance(
                routerId, report.getNameserviceId(), report.getNamenodeId(),
                report.getClusterId(), report.getBlockPoolId(),
                NetUtils.normalizeIP2HostName(report.getRpcAddress()),
                report.getServiceAddress(), report.getLifelineAddress(),
                report.getWebScheme(), report.getWebAddress(), report.getState(),
                report.getSafemode());

        // 由 namenodeStatusReport提取成 membershipStats,

        // 再 membershipState.setStats(membershipStats);
        // namenodeHeartbeatRequest.setNamenodeMembership(membershipState);
        if (report.statsValid()) {
            MembershipStats stats = MembershipStats.newInstance();
            stats.setNumOfFiles(report.getNumFiles());
            stats.setNumOfBlocks(report.getNumBlocks());
            stats.setNumOfBlocksMissing(report.getNumBlocksMissing());
            stats.setNumOfBlocksPendingReplication(
                    report.getNumOfBlocksPendingReplication());
            stats.setNumOfBlocksUnderReplicated(
                    report.getNumOfBlocksUnderReplicated());
            stats.setNumOfBlocksPendingDeletion(
                    report.getNumOfBlocksPendingDeletion());
            stats.setAvailableSpace(report.getAvailableSpace());
            stats.setTotalSpace(report.getTotalSpace());
            stats.setProvidedSpace(report.getProvidedSpace());
            stats.setNumOfDecommissioningDatanodes(
                    report.getNumDecommissioningDatanodes());
            stats.setNumOfActiveDatanodes(report.getNumLiveDatanodes());
            stats.setNumOfDeadDatanodes(report.getNumDeadDatanodes());
            stats.setNumOfStaleDatanodes(report.getNumStaleDatanodes());
            stats.setNumOfDecomActiveDatanodes(report.getNumDecomLiveDatanodes());
            stats.setNumOfDecomDeadDatanodes(report.getNumDecomDeadDatanodes());
            stats.setNumOfInMaintenanceLiveDataNodes(
                    report.getNumInMaintenanceLiveDataNodes());
            stats.setNumOfInMaintenanceDeadDataNodes(
                    report.getNumInMaintenanceDeadDataNodes());
            stats.setNumOfEnteringMaintenanceDataNodes(
                    report.getNumEnteringMaintenanceDataNodes());
            record.setStats(stats);
        }

        if (report.getState() != UNAVAILABLE) {
            record.setLastContact(Time.now());
        }

        NamenodeHeartbeatRequest request = NamenodeHeartbeatRequest.newInstance();
        request.setNamenodeMembership(record);
        return getMembershipStore().namenodeHeartbeat(request).getResult();
    }

    // 获得所有启用的 ns
    @Override
    public Set<FederationNamespaceInfo> getNamespaces() throws IOException {
        GetNamespaceInfoRequest request = GetNamespaceInfoRequest.newInstance();
        GetNamespaceInfoResponse response = getMembershipStore().getNamespaceInfo(request);
        Set<FederationNamespaceInfo> nss = response.getNamespaceInfo();

        Set<FederationNamespaceInfo> ret = new TreeSet<>();
        Set<String> disabled = getDisabledNamespaces();
        for (FederationNamespaceInfo ns : nss) {
            if (!disabled.contains(ns.getNameserviceId())) {
                ret.add(ns);
            }
        }
        return ret;
    }

    @Override
    public Set<String> getDisabledNamespaces() throws IOException {
        DisabledNameserviceStore store = getDisabledNameserviceStore();
        return store.getDisabledNameservices();
    }

    @Override
    public void setRouterId(String router) {
        this.routerId = router;
    }
}
