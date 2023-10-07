package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.NameNodeProxiesClient.ProxyAndInfo;
import org.apache.hadoop.ipc.RPC;

import java.net.InetSocketAddress;

// 1个连接对象(for 一个"逻辑用户")，还会在不活跃（counter为0）的时候自动关闭
// 有一个 counter记录着多少线程使用此连接
public class ConnectionContext {

    private final ProxyAndInfo<?> client;
    // 有多少线程正常使用此连接
    private int numThreads = 0;
    private boolean closed = false;


    public ConnectionContext(ProxyAndInfo<?> connection) {
        this.client = connection;
    }

    public synchronized boolean isActive() {
        return this.numThreads > 0;
    }

    public synchronized boolean isClosed() {
        return this.closed;
    }

    public synchronized boolean isUsable() {
        return !isActive() && !isClosed();
    }

    public synchronized ProxyAndInfo<?> getClient() {
        this.numThreads++;
        return this.client;
    }

    public synchronized void release() {
        if (--this.numThreads == 0 && this.closed) {
            close();
        }
    }

    public synchronized void close() {
        this.closed = true;
        if (this.numThreads == 0) {
            Object proxy = this.client.getProxy();
            RPC.stopProxy(proxy);
        }
    }

    @Override
    public String toString() {
        InetSocketAddress addr = this.client.getAddress();
        Object proxy = this.client.getProxy();
        Class<?> clazz = proxy.getClass();

        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getSimpleName())
                .append("@")
                .append(addr)
                .append("x")
                .append(numThreads);
        if (closed) {
            sb.append("[CLOSED]");
        }
        return sb.toString();
    }
}
