package org.apache.hadoop.hdfs.server.federation.store.impl;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeServiceState;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamespaceInfo;
import org.apache.hadoop.hdfs.server.federation.store.MembershipStore;
import org.apache.hadoop.hdfs.server.federation.store.StateStoreCache;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreDriver;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.hdfs.server.federation.store.records.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.hadoop.hdfs.server.federation.store.StateStoreUtils.filterMultiple;

/**
 * Implementation of the {@link MembershipStore} State Store API.
 */
@InterfaceAudience.Private
@InterfaceStability.Evolving
public class MembershipStoreImpl
        extends MembershipStore implements StateStoreCache {

    private static final Logger LOG =
            LoggerFactory.getLogger(MembershipStoreImpl.class);


    /** Reported namespaces that are not decommissioned. */
    private final Set<FederationNamespaceInfo> activeNamespaces;

    /** Namenodes (after evaluating the quorum) that are active in the cluster. */
    private final Map<String, MembershipState> activeRegistrations;
    /** Namenode status reports (raw) that were discarded for being too old. */
    private final Map<String, MembershipState> expiredRegistrations;

    /** Lock to access the local memory cache. */
    private final ReadWriteLock cacheReadWriteLock =
            new ReentrantReadWriteLock();
    private final Lock cacheReadLock = cacheReadWriteLock.readLock();
    private final Lock cacheWriteLock = cacheReadWriteLock.writeLock();


    public MembershipStoreImpl(StateStoreDriver driver) {
        super(driver);

        this.activeRegistrations = new HashMap<>();
        this.expiredRegistrations = new HashMap<>();
        this.activeNamespaces = new TreeSet<>();
    }

    @Override
    public GetNamenodeRegistrationsResponse getExpiredNamenodeRegistrations(
            GetNamenodeRegistrationsRequest request) throws IOException {

        GetNamenodeRegistrationsResponse response =
                GetNamenodeRegistrationsResponse.newInstance();
        cacheReadLock.lock();
        try {
            Collection<MembershipState> vals = this.expiredRegistrations.values();
            List<MembershipState> copyVals = new ArrayList<>(vals);
            response.setNamenodeMemberships(copyVals);
        } finally {
            cacheReadLock.unlock();
        }
        return response;
    }

    @Override
    public GetNamespaceInfoResponse getNamespaceInfo(
            GetNamespaceInfoRequest request) throws IOException {

        Set<FederationNamespaceInfo> namespaces = new HashSet<>();
        try {
            cacheReadLock.lock();
            namespaces.addAll(activeNamespaces);
        } finally {
            cacheReadLock.unlock();
        }

        return GetNamespaceInfoResponse.newInstance(namespaces);
    }

    @Override
    public GetNamenodeRegistrationsResponse getNamenodeRegistrations(
            final GetNamenodeRegistrationsRequest request) throws IOException {

        // TODO Cache some common queries and sorts
        List<MembershipState> ret = null;

        cacheReadLock.lock();
        try {
            Collection<MembershipState> registrations = activeRegistrations.values();
            MembershipState partialMembership = request.getPartialMembership();
            if (partialMembership == null) {
                ret = new ArrayList<>(registrations);
            } else {
                Query<MembershipState> query = new Query<>(partialMembership);
                ret = filterMultiple(query, registrations);
            }
        } finally {
            cacheReadLock.unlock();
        }
        // Sort in ascending update date order
        Collections.sort(ret);

        return GetNamenodeRegistrationsResponse.newInstance(ret);
    }

    @Override
    public NamenodeHeartbeatResponse namenodeHeartbeat(
            NamenodeHeartbeatRequest request) throws IOException {

        MembershipState record = request.getNamenodeMembership();
        String nnId = record.getNamenodeKey();
        MembershipState existingEntry;
        cacheReadLock.lock();
        try {
            existingEntry = this.activeRegistrations.get(nnId);
        } finally {
            cacheReadLock.unlock();
        }

        if (existingEntry != null) {
            if (existingEntry.getState() != record.getState()) {
                LOG.info("NN registration state has changed: {} -> {}",
                        existingEntry, record);
            } else {
                LOG.debug("Updating NN registration: {} -> {}", existingEntry, record);
            }
        } else {
            LOG.info("Inserting new NN registration: {}", record);
        }

        boolean status = getDriver().put(record, true, false);

        return NamenodeHeartbeatResponse.newInstance(status);
    }

    @Override
    public boolean loadCache(boolean force) throws IOException {
        super.loadCache(force);

        // Update local cache atomically
        cacheWriteLock.lock();
        try {
            this.activeRegistrations.clear();
            this.expiredRegistrations.clear();
            this.activeNamespaces.clear();

            // Build list of NN registrations: nnId -> registration list
            Map<String, List<MembershipState>> nnRegistrations = new HashMap<>();
            List<MembershipState> cachedRecords = getCachedRecords();
            for (MembershipState membership : cachedRecords) {
                String nnId = membership.getNamenodeKey();
                if (membership.getState() == FederationNamenodeServiceState.EXPIRED) {
                    // Expired, RPC service does not use these
                    String key = membership.getPrimaryKey();
                    this.expiredRegistrations.put(key, membership);
                } else {
                    // This is a valid NN registration, build a list of all registrations
                    // using the NN id to use for the quorum calculation.
                    List<MembershipState> nnRegistration = nnRegistrations.computeIfAbsent(nnId, k -> new LinkedList<>());
                    nnRegistration.add(membership);
                    if (membership.getState()
                            != FederationNamenodeServiceState.UNAVAILABLE) {
                        String bpId = membership.getBlockPoolId();
                        String cId = membership.getClusterId();
                        String nsId = membership.getNameserviceId();
                        FederationNamespaceInfo nsInfo =
                                new FederationNamespaceInfo(bpId, cId, nsId);
                        this.activeNamespaces.add(nsInfo);
                    }
                }
            }

            // Calculate most representative entry for each active NN id
            for (List<MembershipState> nnRegistration : nnRegistrations.values()) {
                // Run quorum based on NN state
                MembershipState representativeRecord =
                        getRepresentativeQuorum(nnRegistration);
                String nnKey = representativeRecord.getNamenodeKey();
                this.activeRegistrations.put(nnKey, representativeRecord);
            }
            LOG.debug("Refreshed {} NN registrations from State Store",
                    cachedRecords.size());
        } finally {
            cacheWriteLock.unlock();
        }
        return true;
    }

    @Override
    public UpdateNamenodeRegistrationResponse updateNamenodeRegistration(
            UpdateNamenodeRegistrationRequest request) throws IOException {

        boolean status = false;
        cacheWriteLock.lock();
        try {
            String namenode = MembershipState.getNamenodeKey(
                    request.getNameserviceId(), request.getNamenodeId());
            MembershipState member = this.activeRegistrations.get(namenode);
            if (member != null) {
                member.setState(request.getState());
                status = true;
            }
        } finally {
            cacheWriteLock.unlock();
        }
        return UpdateNamenodeRegistrationResponse.newInstance(status);
    }

    /**
     * Picks the most recent entry in the subset that is most agreeable on the
     * specified field. 1) If a majority of the collection has the same value for
     * the field, the first sorted entry within the subset the matches the
     * majority value 2) Otherwise the first sorted entry in the set of all
     * entries
     *
     * @param records - Collection of state store record objects of the same type
     * @return record that is most representative of the field name
     */
    private MembershipState getRepresentativeQuorum(
            Collection<MembershipState> records) {

        // Collate objects by field value: field value -> order set of records
        Map<FederationNamenodeServiceState, TreeSet<MembershipState>> occurenceMap =
                new HashMap<>();
        for (MembershipState record : records) {
            FederationNamenodeServiceState state = record.getState();
            TreeSet<MembershipState> matchingSet = occurenceMap.computeIfAbsent(state, k -> new TreeSet<>());
            // TreeSet orders elements by descending date via comparators
            matchingSet.add(record);
        }

        // Select largest group
        TreeSet<MembershipState> largestSet = new TreeSet<>();
        for (TreeSet<MembershipState> matchingSet : occurenceMap.values()) {
            if (largestSet.size() < matchingSet.size()) {
                largestSet = matchingSet;
            }
        }

        // If quorum, use the newest element here
        if (largestSet.size() > records.size() / 2) {
            return largestSet.first();
            // Otherwise, return most recent by class comparator
        } else if (records.size() > 0) {
            TreeSet<MembershipState> sortedList = new TreeSet<>(records);
            LOG.debug("Quorum failed, using most recent: {}", sortedList.first());
            return sortedList.first();
        } else {
            return null;
        }
    }
}
