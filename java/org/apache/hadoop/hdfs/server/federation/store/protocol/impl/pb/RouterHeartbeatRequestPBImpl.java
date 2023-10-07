package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterHeartbeatRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterHeartbeatRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterHeartbeatRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterRecordProto;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RouterHeartbeatRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.RouterStatePBImpl;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RouterHeartbeatRequest.
 */
public class RouterHeartbeatRequestPBImpl extends RouterHeartbeatRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<RouterHeartbeatRequestProto, Builder,
            RouterHeartbeatRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<RouterHeartbeatRequestProto,
                    Builder, RouterHeartbeatRequestProtoOrBuilder>(
                    RouterHeartbeatRequestProto.class);

    public RouterHeartbeatRequestPBImpl() {
    }

    @Override
    public RouterHeartbeatRequestProto getProto() {
        return this.translator.build();
    }

    @Override
    public void setProto(Message proto) {
        this.translator.setProto(proto);
    }

    @Override
    public void readInstance(String base64String) throws IOException {
        this.translator.readInstance(base64String);
    }

    @Override
    public RouterState getRouter() throws IOException {
        RouterRecordProto routerProto =
                this.translator.getProtoOrBuilder().getRouter();
        return new RouterStatePBImpl(routerProto);
    }

    @Override
    public void setRouter(RouterState routerState) {
        if (routerState instanceof RouterStatePBImpl) {
            RouterStatePBImpl routerStatePB = (RouterStatePBImpl) routerState;
            this.translator.getBuilder().setRouter(routerStatePB.getProto());
        }
    }
}
