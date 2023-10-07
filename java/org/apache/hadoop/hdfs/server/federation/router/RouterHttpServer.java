package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSUtil;
import org.apache.hadoop.hdfs.server.common.JspHelper;
import org.apache.hadoop.hdfs.server.namenode.NameNodeHttpServer;
import org.apache.hadoop.http.HttpServer2;
import org.apache.hadoop.service.AbstractService;

import javax.servlet.ServletContext;
import java.net.InetSocketAddress;

// 提供 WebUI 的查询服务
// 启动一个 http服务器，底层调用 RouterWebHdfsMethods
public class RouterHttpServer extends AbstractService {

    protected static final String NAMENODE_ATTRIBUTE_KEY = "name.node";

    private Configuration conf;

    private final Router router;

    private HttpServer2 httpServer;

    private InetSocketAddress httpAddress;

    private InetSocketAddress httpsAddress;

    public RouterHttpServer(Router router) {
        super(RouterHttpServer.class.getName());
        this.router = router;
    }

    @Override
    protected void serviceInit(Configuration configuration) throws Exception {
        this.conf = configuration;

        // Get HTTP address
        this.httpAddress = conf.getSocketAddr(
                RBFConfigKeys.DFS_ROUTER_HTTP_BIND_HOST_KEY,
                RBFConfigKeys.DFS_ROUTER_HTTP_ADDRESS_KEY,
                RBFConfigKeys.DFS_ROUTER_HTTP_ADDRESS_DEFAULT,
                RBFConfigKeys.DFS_ROUTER_HTTP_PORT_DEFAULT);

        // Get HTTPs address
        this.httpsAddress = conf.getSocketAddr(
                RBFConfigKeys.DFS_ROUTER_HTTPS_BIND_HOST_KEY,
                RBFConfigKeys.DFS_ROUTER_HTTPS_ADDRESS_KEY,
                RBFConfigKeys.DFS_ROUTER_HTTPS_ADDRESS_DEFAULT,
                RBFConfigKeys.DFS_ROUTER_HTTPS_PORT_DEFAULT);

        super.serviceInit(conf);
    }

    @Override
    protected void serviceStart() throws Exception {
        String webApp = "router";
        HttpServer2.Builder builder = DFSUtil.httpServerTemplateForNNAndJN(
                this.conf, this.httpAddress, this.httpsAddress, webApp,
                RBFConfigKeys.DFS_ROUTER_KERBEROS_INTERNAL_SPNEGO_PRINCIPAL_KEY,
                RBFConfigKeys.DFS_ROUTER_KEYTAB_FILE_KEY);

        this.httpServer = builder.build();

        String httpKeytab = conf.get(DFSUtil.getSpnegoKeytabKey(conf,
                RBFConfigKeys.DFS_ROUTER_KEYTAB_FILE_KEY));
        NameNodeHttpServer.initWebHdfs(conf, httpAddress.getHostName(), httpKeytab,
                httpServer, RouterWebHdfsMethods.class.getPackage().getName());

        this.httpServer.setAttribute(NAMENODE_ATTRIBUTE_KEY, this.router);
        this.httpServer.setAttribute(JspHelper.CURRENT_CONF, this.conf);
        setupServlets(this.httpServer);

        this.httpServer.start();

        // The server port can be ephemeral... ensure we have the correct info
        InetSocketAddress listenAddress = this.httpServer.getConnectorAddress(0);
        if (listenAddress != null) {
            this.httpAddress = new InetSocketAddress(this.httpAddress.getHostName(),
                    listenAddress.getPort());
        }
        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        if (this.httpServer != null) {
            this.httpServer.stop();
        }
        super.serviceStop();
    }

    // 添加 Servlet处理类
    private static void setupServlets(HttpServer2 httpServer) {
        httpServer.addInternalServlet(IsRouterActiveServlet.SERVLET_NAME,
                IsRouterActiveServlet.PATH_SPEC,
                IsRouterActiveServlet.class);
        httpServer.addInternalServlet(RouterFsckServlet.SERVLET_NAME,
                RouterFsckServlet.PATH_SPEC,
                RouterFsckServlet.class,
                true);
        httpServer.addInternalServlet(RouterNetworkTopologyServlet.SERVLET_NAME,
                RouterNetworkTopologyServlet.PATH_SPEC,
                RouterNetworkTopologyServlet.class);
    }

    public InetSocketAddress getHttpAddress() {
        return this.httpAddress;
    }

    public InetSocketAddress getHttpsAddress() {
        return this.httpsAddress;
    }

    static Configuration getConfFromContext(ServletContext context) {
        return (Configuration) context.getAttribute(JspHelper.CURRENT_CONF);
    }

    public static Router getRouterFromContext(ServletContext context) {
        return (Router) context.getAttribute(NAMENODE_ATTRIBUTE_KEY);
    }
}