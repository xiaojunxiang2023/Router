package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.metrics2.MetricsSystem;
import org.apache.hadoop.metrics2.annotation.Metric;
import org.apache.hadoop.metrics2.annotation.Metrics;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.metrics2.lib.MetricsRegistry;
import org.apache.hadoop.metrics2.lib.MutableGaugeInt;
import org.apache.hadoop.metrics2.source.JvmMetrics;

import static org.apache.hadoop.metrics2.impl.MsInfo.ProcessName;
import static org.apache.hadoop.metrics2.impl.MsInfo.SessionId;

@Metrics(name = "RouterActivity", about = "Router metrics", context = "dfs")
public class RouterMetrics {

    private final MetricsRegistry registry = new MetricsRegistry("router");

    @Metric("Duration in SafeMode at startup in msec")
    private MutableGaugeInt safeModeTime;

    private JvmMetrics jvmMetrics;

    RouterMetrics(String processName, String sessionId, final JvmMetrics jvmMetrics) {
        this.jvmMetrics = jvmMetrics;
        registry.tag(ProcessName, processName).tag(SessionId, sessionId);
    }

    public static RouterMetrics create(Configuration conf) {
        String sessionId = conf.get(DFSConfigKeys.DFS_METRICS_SESSION_ID_KEY);
        String processName = "Router";
        MetricsSystem ms = DefaultMetricsSystem.instance();
        JvmMetrics jm = JvmMetrics.create(processName, sessionId, ms);

        return ms.register(new RouterMetrics(processName, sessionId, jm));
    }

    public JvmMetrics getJvmMetrics() {
        return jvmMetrics;
    }

    public void shutdown() {
        DefaultMetricsSystem.shutdown();
    }

    public void setSafeModeTime(long elapsed) {
        safeModeTime.set((int) elapsed);
    }
}
