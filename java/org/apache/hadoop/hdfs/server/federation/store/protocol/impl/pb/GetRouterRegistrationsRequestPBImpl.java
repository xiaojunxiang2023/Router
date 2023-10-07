package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationsRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationsRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationsRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetRouterRegistrationsRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetRouterRegistrationsRequest.
 */
public class GetRouterRegistrationsRequestPBImpl
        extends GetRouterRegistrationsRequest implements PBRecord {

    private FederationProtocolPBTranslator<GetRouterRegistrationsRequestProto,
            Builder, GetRouterRegistrationsRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<
                    GetRouterRegistrationsRequestProto, Builder,
                    GetRouterRegistrationsRequestProtoOrBuilder>(
                    GetRouterRegistrationsRequestProto.class);

    public GetRouterRegistrationsRequestPBImpl() {
    }

    @Override
    public GetRouterRegistrationsRequestProto getProto() {
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
}