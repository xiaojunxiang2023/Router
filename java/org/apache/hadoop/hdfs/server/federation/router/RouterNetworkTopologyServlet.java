package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.server.namenode.NetworkTopologyServlet;
import org.apache.hadoop.net.Node;
import org.apache.hadoop.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

// /topology
// 打印 DataNode的拓扑信息
// 靠 router.getRpcServer().getDatanodeReport()，底层又请求 NameNode
public class RouterNetworkTopologyServlet extends NetworkTopologyServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        final ServletContext context = getServletContext();

        String format = parseAcceptHeader(request);
        if (FORMAT_TEXT.equals(format)) {
            response.setContentType("text/plain; charset=UTF-8");
        } else if (FORMAT_JSON.equals(format)) {
            response.setContentType("application/json; charset=UTF-8");
        }

        Router router = RouterHttpServer.getRouterFromContext(context);
        DatanodeInfo[] datanodeReport = router.getRpcServer().getDatanodeReport(HdfsConstants.DatanodeReportType.ALL);
        List<Node> datanodeInfos = Arrays.asList(datanodeReport);

        try (PrintStream out = new PrintStream(response.getOutputStream(), false, "UTF-8")) {
            printTopology(out, datanodeInfos, format);
        } catch (Throwable t) {
            String errMsg = "Print network topology failed. " + StringUtils.stringifyException(t);
            response.sendError(HttpServletResponse.SC_GONE, errMsg);
            throw new IOException(errMsg);
        } finally {
            response.getOutputStream().close();
        }
    }
}