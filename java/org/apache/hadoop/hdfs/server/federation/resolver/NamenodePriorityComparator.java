package org.apache.hadoop.hdfs.server.federation.resolver;

import java.io.Serializable;
import java.util.Comparator;

// 比较器，根据 Active、StandBy 等状态去比较，如果相同的话就比最近更新时间
public class NamenodePriorityComparator
        implements Comparator<FederationNamenodeContext>, Serializable {

    private static final long serialVersionUID = 2304924292036293331L;

    @Override
    public int compare(FederationNamenodeContext o1,
                       FederationNamenodeContext o2) {
        FederationNamenodeServiceState state1 = o1.getState();
        FederationNamenodeServiceState state2 = o2.getState();

        if (state1 == state2) {
            // Both have the same state, use mode dates
            return compareModDates(o1, o2);
        } else {
            // Enum is ordered by priority
            return state1.compareTo(state2);
        }
    }

    private int compareModDates(FederationNamenodeContext o1,
                                FederationNamenodeContext o2) {
        // Reverse sort, lowest position is highest priority.
        return (int) (o2.getDateModified() - o1.getDateModified());
    }
}
