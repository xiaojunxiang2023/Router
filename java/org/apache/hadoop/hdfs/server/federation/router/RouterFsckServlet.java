package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.common.JspHelper;
import org.apache.hadoop.security.UserGroupInformation;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.security.PrivilegedExceptionAction;
import java.util.Map;


// /fsck 
// 底层调用了 RouterFsck, 底层又请求 NameNode
@InterfaceAudience.Private
public class RouterFsckServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String SERVLET_NAME = "fsck";
    public static final String PATH_SPEC = "/fsck";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final Map<String, String[]> pmap = request.getParameterMap();
        final PrintWriter out = response.getWriter();
        final InetAddress remoteAddress =
                InetAddress.getByName(request.getRemoteAddr());
        final ServletContext context = getServletContext();
        final Configuration conf = RouterHttpServer.getConfFromContext(context);
        final UserGroupInformation ugi = getUGI(request, conf);
        try {
            ugi.doAs((PrivilegedExceptionAction<Object>) () -> {
                Router router = RouterHttpServer.getRouterFromContext(context);
                // 调用 RouterFsck
                new RouterFsck(router, pmap, out, remoteAddress).fsck();
                return null;
            });
        } catch (InterruptedException e) {
            response.sendError(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
        }
    }

    protected UserGroupInformation getUGI(HttpServletRequest request,
                                          Configuration conf) throws IOException {
        return JspHelper.getUGI(getServletContext(), request, conf);
    }
}
