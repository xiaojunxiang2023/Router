package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.protocolPB.RouterAdminProtocolPB;
import org.apache.hadoop.hdfs.protocolPB.RouterAdminProtocolTranslatorPB;
import org.apache.hadoop.hdfs.server.federation.resolver.MountTableManager;
import org.apache.hadoop.hdfs.server.federation.resolver.RouterGenericManager;
import org.apache.hadoop.ipc.ProtobufRpcEngine2;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

// Admin所用的客户端
@Private
public class RouterClient implements Closeable {

    private final RouterAdminProtocolTranslatorPB proxy;
    private final UserGroupInformation ugi;

    private static RouterAdminProtocolTranslatorPB createRouterProxy(InetSocketAddress address, Configuration conf, UserGroupInformation ugi)
            throws IOException {

        RPC.setProtocolEngine(
                conf, RouterAdminProtocolPB.class, ProtobufRpcEngine2.class);

        AtomicBoolean fallbackToSimpleAuth = new AtomicBoolean(false);
        final long version = RPC.getProtocolVersion(RouterAdminProtocolPB.class);
        RouterAdminProtocolPB proxy = RPC.getProtocolProxy(
                RouterAdminProtocolPB.class, version, address, ugi, conf,
                NetUtils.getDefaultSocketFactory(conf),
                RPC.getRpcTimeout(conf), null,
                fallbackToSimpleAuth).getProxy();

        return new RouterAdminProtocolTranslatorPB(proxy);
    }

    public RouterClient(InetSocketAddress address, Configuration conf) throws IOException {
        this.ugi = UserGroupInformation.getCurrentUser();
        this.proxy = createRouterProxy(address, conf, ugi);
    }

    public MountTableManager getMountTableManager() {
        return proxy;
    }

    public RouterStateManager getRouterStateManager() {
        return proxy;
    }

    public NameserviceManager getNameserviceManager() {
        return proxy;
    }

    public RouterGenericManager getRouterGenericManager() {
        return proxy;
    }

    @Override
    public synchronized void close() {
        RPC.stopProxy(proxy);
    }
}