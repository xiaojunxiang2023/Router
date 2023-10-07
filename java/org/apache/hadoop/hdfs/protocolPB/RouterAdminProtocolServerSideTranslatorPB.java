package org.apache.hadoop.hdfs.protocolPB;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.*;
import org.apache.hadoop.hdfs.server.federation.router.RouterAdminServer;
import org.apache.hadoop.hdfs.server.federation.store.protocol.*;
import org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb.*;
import org.apache.hadoop.thirdparty.protobuf.RpcController;
import org.apache.hadoop.thirdparty.protobuf.ServiceException;

import java.io.IOException;

// 服务端的数据转换器（适配器）
@InterfaceAudience.Private
@InterfaceStability.Stable
public class RouterAdminProtocolServerSideTranslatorPB implements
        RouterAdminProtocolPB {

    private final RouterAdminServer server;

    /**
     * Constructor.
     * @param server The NN server.
     * @throws IOException if it cannot create the translator.
     */
    public RouterAdminProtocolServerSideTranslatorPB(RouterAdminServer server)
            throws IOException {
        this.server = server;
    }

    @Override
    public AddMountTableEntryResponseProto addMountTableEntry(
            RpcController controller, AddMountTableEntryRequestProto request)
            throws ServiceException {

        try {
            AddMountTableEntryRequest req =
                    new AddMountTableEntryRequestPBImpl(request);
            AddMountTableEntryResponse response = server.addMountTableEntry(req);
            AddMountTableEntryResponsePBImpl responsePB =
                    (AddMountTableEntryResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public RemoveMountTableEntryResponseProto removeMountTableEntry(
            RpcController controller, RemoveMountTableEntryRequestProto request)
            throws ServiceException {
        try {
            RemoveMountTableEntryRequest req =
                    new RemoveMountTableEntryRequestPBImpl(request);
            RemoveMountTableEntryResponse response =
                    server.removeMountTableEntry(req);
            RemoveMountTableEntryResponsePBImpl responsePB =
                    (RemoveMountTableEntryResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public GetMountTableEntriesResponseProto getMountTableEntries(
            RpcController controller, GetMountTableEntriesRequestProto request)
            throws ServiceException {
        try {
            GetMountTableEntriesRequest req =
                    new GetMountTableEntriesRequestPBImpl(request);
            GetMountTableEntriesResponse response = server.getMountTableEntries(req);
            GetMountTableEntriesResponsePBImpl responsePB =
                    (GetMountTableEntriesResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public UpdateMountTableEntryResponseProto updateMountTableEntry(
            RpcController controller, UpdateMountTableEntryRequestProto request)
            throws ServiceException {
        try {
            UpdateMountTableEntryRequest req =
                    new UpdateMountTableEntryRequestPBImpl(request);
            UpdateMountTableEntryResponse response =
                    server.updateMountTableEntry(req);
            UpdateMountTableEntryResponsePBImpl responsePB =
                    (UpdateMountTableEntryResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public RefreshSuperUserGroupsConfigurationResponseProto
    refreshSuperUserGroupsConfiguration(
            RpcController controller,
            RefreshSuperUserGroupsConfigurationRequestProto request)
            throws ServiceException {
        try {
            boolean result = server.refreshSuperUserGroupsConfiguration();
            RefreshSuperUserGroupsConfigurationResponse response =
                    RefreshSuperUserGroupsConfigurationResponsePBImpl.newInstance(result);
            RefreshSuperUserGroupsConfigurationResponsePBImpl responsePB =
                    (RefreshSuperUserGroupsConfigurationResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }


    @Override
    public EnterSafeModeResponseProto enterSafeMode(RpcController controller,
                                                    EnterSafeModeRequestProto request) throws ServiceException {
        try {
            EnterSafeModeRequest req = new EnterSafeModeRequestPBImpl(request);
            EnterSafeModeResponse response = server.enterSafeMode(req);
            EnterSafeModeResponsePBImpl responsePB =
                    (EnterSafeModeResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public LeaveSafeModeResponseProto leaveSafeMode(RpcController controller,
                                                    LeaveSafeModeRequestProto request) throws ServiceException {
        try {
            LeaveSafeModeRequest req = new LeaveSafeModeRequestPBImpl(request);
            LeaveSafeModeResponse response = server.leaveSafeMode(req);
            LeaveSafeModeResponsePBImpl responsePB =
                    (LeaveSafeModeResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public GetSafeModeResponseProto getSafeMode(RpcController controller,
                                                GetSafeModeRequestProto request) throws ServiceException {
        try {
            GetSafeModeRequest req = new GetSafeModeRequestPBImpl(request);
            GetSafeModeResponse response = server.getSafeMode(req);
            GetSafeModeResponsePBImpl responsePB =
                    (GetSafeModeResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public DisableNameserviceResponseProto disableNameservice(
            RpcController controller, DisableNameserviceRequestProto request)
            throws ServiceException {
        try {
            DisableNameserviceRequest req =
                    new DisableNameserviceRequestPBImpl(request);
            DisableNameserviceResponse response = server.disableNameservice(req);
            DisableNameserviceResponsePBImpl responsePB =
                    (DisableNameserviceResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public EnableNameserviceResponseProto enableNameservice(
            RpcController controller, EnableNameserviceRequestProto request)
            throws ServiceException {
        try {
            EnableNameserviceRequest req =
                    new EnableNameserviceRequestPBImpl(request);
            EnableNameserviceResponse response = server.enableNameservice(req);
            EnableNameserviceResponsePBImpl responsePB =
                    (EnableNameserviceResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public GetDisabledNameservicesResponseProto getDisabledNameservices(
            RpcController controller, GetDisabledNameservicesRequestProto request)
            throws ServiceException {
        try {
            GetDisabledNameservicesRequest req =
                    new GetDisabledNameservicesRequestPBImpl(request);
            GetDisabledNameservicesResponse response =
                    server.getDisabledNameservices(req);
            GetDisabledNameservicesResponsePBImpl responsePB =
                    (GetDisabledNameservicesResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public RefreshMountTableEntriesResponseProto refreshMountTableEntries(
            RpcController controller, RefreshMountTableEntriesRequestProto request)
            throws ServiceException {
        try {
            RefreshMountTableEntriesRequest req =
                    new RefreshMountTableEntriesRequestPBImpl(request);
            RefreshMountTableEntriesResponse response =
                    server.refreshMountTableEntries(req);
            RefreshMountTableEntriesResponsePBImpl responsePB =
                    (RefreshMountTableEntriesResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public GetDestinationResponseProto getDestination(
            RpcController controller, GetDestinationRequestProto request)
            throws ServiceException {
        try {
            GetDestinationRequest req =
                    new GetDestinationRequestPBImpl(request);
            GetDestinationResponse response = server.getDestination(req);
            GetDestinationResponsePBImpl responsePB =
                    (GetDestinationResponsePBImpl) response;
            return responsePB.getProto();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }
}
