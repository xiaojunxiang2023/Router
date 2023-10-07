package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.federation.metrics.NamenodeBeanMetrics;
import org.apache.hadoop.hdfs.server.federation.metrics.RBFMetrics;
import org.apache.hadoop.metrics2.source.JvmMetrics;
import org.apache.hadoop.service.AbstractService;

// 管理多个 Metrcis
public class RouterMetricsService extends AbstractService {

    private final Router router;

    private RouterMetrics routerMetrics;
    private RBFMetrics rbfMetrics;
    private NamenodeBeanMetrics nnMetrics;


    public RouterMetricsService(final Router router) {
        super(RouterMetricsService.class.getName());
        this.router = router;
    }

    @Override
    protected void serviceInit(Configuration configuration) throws Exception {
        this.routerMetrics = RouterMetrics.create(configuration);
    }

    @Override
    protected void serviceStart() throws Exception {
        this.nnMetrics = new NamenodeBeanMetrics(this.router);
        this.rbfMetrics = new RBFMetrics(this.router);
    }

    @Override
    protected void serviceStop() throws Exception {
        if (this.rbfMetrics != null) {
            this.rbfMetrics.close();
        }

        if (this.nnMetrics != null) {
            this.nnMetrics.close();
        }

        if (this.routerMetrics != null) {
            this.routerMetrics.shutdown();
        }
    }

    public RouterMetrics getRouterMetrics() {
        return this.routerMetrics;
    }

    public RBFMetrics getRBFMetrics() {
        return this.rbfMetrics;
    }

    public NamenodeBeanMetrics getNamenodeMetrics() {
        return this.nnMetrics;
    }

    public JvmMetrics getJvmMetrics() {
        if (this.routerMetrics == null) {
            return null;
        }
        return this.routerMetrics.getJvmMetrics();
    }
}
