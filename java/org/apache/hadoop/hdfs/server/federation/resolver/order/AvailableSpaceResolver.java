package org.apache.hadoop.hdfs.server.federation.resolver.order;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.resolver.PathLocation;
import org.apache.hadoop.hdfs.server.federation.resolver.order.AvailableSpaceResolver.SubclusterAvailableSpace;
import org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys;
import org.apache.hadoop.hdfs.server.federation.router.Router;
import org.apache.hadoop.hdfs.server.federation.store.MembershipStore;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamenodeRegistrationsRequest;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamenodeRegistrationsResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.thirdparty.com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

// 注意，这是个概率值(权重，即 preference的值)，并不是绝对地以空间大小来排序
// 
public class AvailableSpaceResolver
        extends RouterResolver<String, SubclusterAvailableSpace> {

    private static final Logger LOG = LoggerFactory.getLogger(AvailableSpaceResolver.class);

    // 一个阈值参数，值不超过多少才算 状态良好。
    // 默认为 60%
    // 同时相当于比较器的权重概率值
    public static final String BALANCER_PREFERENCE_KEY =
            RBFConfigKeys.FEDERATION_ROUTER_PREFIX
                    + "available-space-resolver.balanced-space-preference-fraction";
    public static final float BALANCER_PREFERENCE_DEFAULT = 0.6f;

    private static final Random RAND = new Random();

    private final SubclusterSpaceComparator comparator;

    public AvailableSpaceResolver(final Configuration conf,
                                  final Router routerService) {
        super(conf, routerService);
        float balancedPreference = conf.getFloat(BALANCER_PREFERENCE_KEY,
                BALANCER_PREFERENCE_DEFAULT);
        // 阈值设得太苛刻了
        if (balancedPreference < 0.5) {
            LOG.warn("The balancer preference value is less than 0.5. That means more"
                    + " files will be allocated in cluster with lower available space.");
        }
        this.comparator = new SubclusterSpaceComparator(balancedPreference);
    }

    // 核心方法实现, 获得最优的 ns
    // 即取 subclusterList.sort(comparator);
    @Override
    protected String chooseFirstNamespace(String path, PathLocation loc) {
        Map<String, SubclusterAvailableSpace> subclusterInfo = getSubclusterMapping();
        List<SubclusterAvailableSpace> subclusterList = new LinkedList<>(subclusterInfo.values());
        subclusterList.sort(comparator);

        return subclusterList.size() > 0 ? subclusterList.get(0).getNameserviceId() : null;
    }

    // id -> SubclusterAvailableSpace[ns, availableSpace]
    @Override
    protected Map<String, SubclusterAvailableSpace> getSubclusterInfo(MembershipStore membershipStore) {
        Map<String, SubclusterAvailableSpace> mapping = new HashMap<>();
        try {
            // 从 membershipStore获取 availableSpace
            GetNamenodeRegistrationsRequest request = GetNamenodeRegistrationsRequest
                    .newInstance();
            GetNamenodeRegistrationsResponse response = membershipStore
                    .getNamenodeRegistrations(request);
            final List<MembershipState> nns = response.getNamenodeMemberships();
            for (MembershipState nn : nns) {
                try {
                    String nsId = nn.getNameserviceId();
                    long availableSpace = nn.getStats().getAvailableSpace();
                    mapping.put(nsId, new SubclusterAvailableSpace(nsId, availableSpace));
                } catch (Exception e) {
                    LOG.error("Cannot get stats info for {}: {}.", nn, e.getMessage());
                }
            }
        } catch (IOException ioe) {
            LOG.error("Cannot get Namenodes from the State Store.", ioe);
        }
        return mapping;
    }


    static class SubclusterAvailableSpace {
        private final String nsId;
        private final long availableSpace;

        SubclusterAvailableSpace(String nsId, long availableSpace) {
            this.nsId = nsId;
            this.availableSpace = availableSpace;
        }

        public String getNameserviceId() {
            return this.nsId;
        }

        public long getAvailableSpace() {
            return this.availableSpace;
        }
    }

    // 比较器
    static final class SubclusterSpaceComparator
            implements Comparator<SubclusterAvailableSpace>, Serializable {
        private final int balancedPreference;

        SubclusterSpaceComparator(float balancedPreference) {
            Preconditions.checkArgument(balancedPreference <= 1 && balancedPreference >= 0,
                    "The balancer preference value should be in the range 0.0 - 1.0");

            this.balancedPreference = (int) (100 * balancedPreference);
        }

        @Override
        public int compare(SubclusterAvailableSpace cluster1,
                           SubclusterAvailableSpace cluster2) {
            int ret = cluster1.getAvailableSpace() > cluster2.getAvailableSpace() ? -1 : 1;

            if (ret < 0) {
                return (RAND.nextInt(100) < balancedPreference) ? -1 : 1;
            } else {
                return (RAND.nextInt(100) < balancedPreference) ? 1 : -1;
            }
        }
    }
}
