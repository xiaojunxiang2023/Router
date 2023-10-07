package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.http.IsActiveServlet;

import javax.servlet.ServletContext;

// /isActive
// 检测 Router是否存活
public class IsRouterActiveServlet extends IsActiveServlet {

    @Override
    protected boolean isActive() {
        final ServletContext context = getServletContext();
        final Router router = RouterHttpServer.getRouterFromContext(context);
        final RouterServiceState routerState = router.getRouterState();

        return routerState == RouterServiceState.RUNNING;
    }
}