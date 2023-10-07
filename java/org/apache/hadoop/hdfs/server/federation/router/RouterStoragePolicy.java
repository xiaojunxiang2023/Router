package org.apache.hadoop.hdfs.server.federation.router;

import org.apache.hadoop.hdfs.protocol.BlockStoragePolicy;
import org.apache.hadoop.hdfs.server.federation.resolver.RemoteLocation;
import org.apache.hadoop.hdfs.server.namenode.NameNode;

import java.io.IOException;
import java.util.List;

// 存储策略相关的向 RPC调用
public class RouterStoragePolicy {

    private final RouterRpcServer rpcServer;
    private final RouterRpcClient rpcClient;

    public RouterStoragePolicy(RouterRpcServer server) {
        this.rpcServer = server;
        this.rpcClient = this.rpcServer.getRPCClient();
    }

    public void setStoragePolicy(String src, String policyName)
            throws IOException {
        rpcServer.checkOperation(NameNode.OperationCategory.WRITE);
        List<RemoteLocation> locations =
                rpcServer.getLocationsForPath(src, false, false);
        RemoteMethod method = new RemoteMethod("setStoragePolicy",
                new Class<?>[]{String.class, String.class},
                new RemoteParam(),
                policyName);
        if (rpcServer.isInvokeConcurrent(src)) {
            rpcClient.invokeConcurrent(locations, method);
        } else {
            rpcClient.invokeSequential(locations, method);
        }
    }

    public BlockStoragePolicy[] getStoragePolicies() throws IOException {
        rpcServer.checkOperation(NameNode.OperationCategory.READ);

        RemoteMethod method = new RemoteMethod("getStoragePolicies");
        return rpcServer.invokeAtAvailableNs(method, BlockStoragePolicy[].class);
    }

    public void unsetStoragePolicy(String src) throws IOException {
        rpcServer.checkOperation(NameNode.OperationCategory.WRITE, true);

        List<RemoteLocation> locations =
                rpcServer.getLocationsForPath(src, false, false);
        RemoteMethod method = new RemoteMethod("unsetStoragePolicy",
                new Class<?>[]{String.class},
                new RemoteParam());
        if (rpcServer.isInvokeConcurrent(src)) {
            rpcClient.invokeConcurrent(locations, method);
        } else {
            rpcClient.invokeSequential(locations, method);
        }
    }

    public BlockStoragePolicy getStoragePolicy(String path)
            throws IOException {
        rpcServer.checkOperation(NameNode.OperationCategory.READ, true);

        List<RemoteLocation> locations =
                rpcServer.getLocationsForPath(path, false, false);
        RemoteMethod method = new RemoteMethod("getStoragePolicy",
                new Class<?>[]{String.class},
                new RemoteParam());
        return (BlockStoragePolicy) rpcClient.invokeSequential(locations, method);
    }

    public void satisfyStoragePolicy(String path) throws IOException {
        rpcServer.checkOperation(NameNode.OperationCategory.READ, true);

        List<RemoteLocation> locations =
                rpcServer.getLocationsForPath(path, true, false);
        RemoteMethod method = new RemoteMethod("satisfyStoragePolicy",
                new Class<?>[]{String.class},
                new RemoteParam());
        rpcClient.invokeSequential(locations, method);
    }
}
