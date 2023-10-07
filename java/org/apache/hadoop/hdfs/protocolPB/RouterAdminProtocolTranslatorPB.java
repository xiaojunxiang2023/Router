package org.apache.hadoop.hdfs.protocolPB;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.*;
import org.apache.hadoop.hdfs.server.federation.resolver.MountTableManager;
import org.apache.hadoop.hdfs.server.federation.resolver.RouterGenericManager;
import org.apache.hadoop.hdfs.server.federation.router.NameserviceManager;
import org.apache.hadoop.hdfs.server.federation.router.RouterStateManager;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb.*;
import org.apache.hadoop.ipc.*;
import org.apache.hadoop.thirdparty.protobuf.ServiceException;

import java.io.Closeable;
import java.io.IOException;

// 客户端的数据转换器（适配器）
@InterfaceAudience.Private
@InterfaceStability.Stable
public class RouterAdminProtocolTranslatorPB
        implements ProtocolMetaInterface, MountTableManager,
        Closeable, ProtocolTranslator, RouterStateManager, NameserviceManager,
        RouterGenericManager {
    final private RouterAdminProtocolPB rpcProxy;

    public RouterAdminProtocolTranslatorPB(RouterAdminProtocolPB proxy) {
        rpcProxy = proxy;
    }

    @Override
    public void close() {
        RPC.stopProxy(rpcProxy);
    }

    @Override
    public Object getUnderlyingProxyObject() {
        return rpcProxy;
    }

    @Override
    public boolean isMethodSupported(String methodName) throws IOException {
        return RpcClientUtil.isMethodSupported(rpcProxy,
                RouterAdminProtocolPB.class, RPC.RpcKind.RPC_PROTOCOL_BUFFER,
                RPC.getProtocolVersion(RouterAdminProtocolPB.class), methodName);
    }

    @Override
    public AddMountTableEntryResponse addMountTableEntry(
            AddMountTableEntryRequest request) throws IOException {
        AddMountTableEntryRequestPBImpl requestPB =
                (AddMountTableEntryRequestPBImpl) request;
        AddMountTableEntryRequestProto proto = requestPB.getProto();
        try {
            AddMountTableEntryResponseProto response =
                    rpcProxy.addMountTableEntry(null, proto);
            return new AddMountTableEntryResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public UpdateMountTableEntryResponse updateMountTableEntry(
            UpdateMountTableEntryRequest request) throws IOException {
        UpdateMountTableEntryRequestPBImpl requestPB =
                (UpdateMountTableEntryRequestPBImpl) request;
        UpdateMountTableEntryRequestProto proto = requestPB.getProto();
        try {
            UpdateMountTableEntryResponseProto response =
                    rpcProxy.updateMountTableEntry(null, proto);
            return new UpdateMountTableEntryResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public RemoveMountTableEntryResponse removeMountTableEntry(
            RemoveMountTableEntryRequest request) throws IOException {
        RemoveMountTableEntryRequestPBImpl requestPB =
                (RemoveMountTableEntryRequestPBImpl) request;
        RemoveMountTableEntryRequestProto proto = requestPB.getProto();
        try {
            RemoveMountTableEntryResponseProto responseProto =
                    rpcProxy.removeMountTableEntry(null, proto);
            return new RemoveMountTableEntryResponsePBImpl(responseProto);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public GetMountTableEntriesResponse getMountTableEntries(
            GetMountTableEntriesRequest request) throws IOException {
        GetMountTableEntriesRequestPBImpl requestPB =
                (GetMountTableEntriesRequestPBImpl) request;
        GetMountTableEntriesRequestProto proto = requestPB.getProto();
        try {
            GetMountTableEntriesResponseProto response =
                    rpcProxy.getMountTableEntries(null, proto);
            return new GetMountTableEntriesResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public EnterSafeModeResponse enterSafeMode(EnterSafeModeRequest request)
            throws IOException {
        EnterSafeModeRequestProto proto =
                EnterSafeModeRequestProto.newBuilder().build();
        try {
            EnterSafeModeResponseProto response =
                    rpcProxy.enterSafeMode(null, proto);
            return new EnterSafeModeResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public LeaveSafeModeResponse leaveSafeMode(LeaveSafeModeRequest request)
            throws IOException {
        LeaveSafeModeRequestProto proto =
                LeaveSafeModeRequestProto.newBuilder().build();
        try {
            LeaveSafeModeResponseProto response =
                    rpcProxy.leaveSafeMode(null, proto);
            return new LeaveSafeModeResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public GetSafeModeResponse getSafeMode(GetSafeModeRequest request)
            throws IOException {
        GetSafeModeRequestProto proto =
                GetSafeModeRequestProto.newBuilder().build();
        try {
            GetSafeModeResponseProto response =
                    rpcProxy.getSafeMode(null, proto);
            return new GetSafeModeResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public DisableNameserviceResponse disableNameservice(
            DisableNameserviceRequest request) throws IOException {
        DisableNameserviceRequestPBImpl requestPB =
                (DisableNameserviceRequestPBImpl) request;
        DisableNameserviceRequestProto proto = requestPB.getProto();
        try {
            DisableNameserviceResponseProto response =
                    rpcProxy.disableNameservice(null, proto);
            return new DisableNameserviceResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public EnableNameserviceResponse enableNameservice(
            EnableNameserviceRequest request) throws IOException {
        EnableNameserviceRequestPBImpl requestPB =
                (EnableNameserviceRequestPBImpl) request;
        EnableNameserviceRequestProto proto = requestPB.getProto();
        try {
            EnableNameserviceResponseProto response =
                    rpcProxy.enableNameservice(null, proto);
            return new EnableNameserviceResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public GetDisabledNameservicesResponse getDisabledNameservices(
            GetDisabledNameservicesRequest request) throws IOException {
        GetDisabledNameservicesRequestProto proto =
                GetDisabledNameservicesRequestProto.newBuilder().build();
        try {
            GetDisabledNameservicesResponseProto response =
                    rpcProxy.getDisabledNameservices(null, proto);
            return new GetDisabledNameservicesResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public RefreshMountTableEntriesResponse refreshMountTableEntries(
            RefreshMountTableEntriesRequest request) throws IOException {
        RefreshMountTableEntriesRequestPBImpl requestPB =
                (RefreshMountTableEntriesRequestPBImpl) request;
        RefreshMountTableEntriesRequestProto proto = requestPB.getProto();
        try {
            RefreshMountTableEntriesResponseProto response =
                    rpcProxy.refreshMountTableEntries(null, proto);
            return new RefreshMountTableEntriesResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public GetDestinationResponse getDestination(
            GetDestinationRequest request) throws IOException {
        GetDestinationRequestPBImpl requestPB =
                (GetDestinationRequestPBImpl) request;
        GetDestinationRequestProto proto = requestPB.getProto();
        try {
            GetDestinationResponseProto response =
                    rpcProxy.getDestination(null, proto);
            return new GetDestinationResponsePBImpl(response);
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }

    @Override
    public boolean refreshSuperUserGroupsConfiguration() throws IOException {
        RefreshSuperUserGroupsConfigurationRequestProto proto =
                RefreshSuperUserGroupsConfigurationRequestProto.newBuilder().build();
        try {
            RefreshSuperUserGroupsConfigurationResponseProto response =
                    rpcProxy.refreshSuperUserGroupsConfiguration(null, proto);
            return new RefreshSuperUserGroupsConfigurationResponsePBImpl(response)
                    .getStatus();
        } catch (ServiceException e) {
            throw new IOException(ProtobufHelper.getRemoteException(e).getMessage());
        }
    }
}
