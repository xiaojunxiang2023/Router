package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.service.CompositeService.CompositeServiceShutdownHook;
import org.apache.hadoop.util.ShutdownHookManager;
import org.apache.hadoop.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.hadoop.util.ExitUtil.terminate;

// 没啥逻辑，其实是靠调用 Router类
public final class DFSRouter {

    private static final Logger LOG = LoggerFactory.getLogger(DFSRouter.class);


    private static final String USAGE = "Usage: hdfs dfsrouter";

    public static final int SHUTDOWN_HOOK_PRIORITY = 30;


    private DFSRouter() {
    }

    public static void main(String[] argv) {
        if (DFSUtil.parseHelpArgument(argv, USAGE, System.out, true)) {
            System.exit(0);
        }

        try {
            StringUtils.startupShutdownMessage(Router.class, argv, LOG);
            Router router = new Router();

            ShutdownHookManager.get().addShutdownHook(new CompositeServiceShutdownHook(router), SHUTDOWN_HOOK_PRIORITY);

            Configuration conf = new HdfsConfiguration();
            router.init(conf);
            router.start();
        } catch (Throwable e) {
            LOG.error("Failed to start router", e);
            terminate(1, e);
        }
    }
    
}
