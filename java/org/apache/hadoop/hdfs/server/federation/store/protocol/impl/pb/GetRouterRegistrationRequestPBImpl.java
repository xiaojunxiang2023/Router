package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetRouterRegistrationRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetRouterRegistrationRequest.
 */
public class GetRouterRegistrationRequestPBImpl
        extends GetRouterRegistrationRequest implements PBRecord {

    private FederationProtocolPBTranslator<GetRouterRegistrationRequestProto,
            Builder, GetRouterRegistrationRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<GetRouterRegistrationRequestProto,
                    Builder, GetRouterRegistrationRequestProtoOrBuilder>(
                    GetRouterRegistrationRequestProto.class);

    public GetRouterRegistrationRequestPBImpl() {
    }

    @Override
    public GetRouterRegistrationRequestProto getProto() {
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
    public String getRouterId() {
        return this.translator.getProtoOrBuilder().getRouterId();
    }

    @Override
    public void setRouterId(String routerId) {
        this.translator.getBuilder().setRouterId(routerId);
    }
}