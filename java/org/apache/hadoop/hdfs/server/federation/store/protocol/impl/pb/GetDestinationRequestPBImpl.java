package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetDestinationRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetDestinationRequest.
 */
public class GetDestinationRequestPBImpl extends GetDestinationRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<GetDestinationRequestProto,
            Builder, GetDestinationRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    GetDestinationRequestProto.class);

    public GetDestinationRequestPBImpl() {
    }

    public GetDestinationRequestPBImpl(GetDestinationRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetDestinationRequestProto getProto() {
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
    public String getSrcPath() {
        return this.translator.getProtoOrBuilder().getSrcPath();
    }

    @Override
    public void setSrcPath(String path) {
        this.translator.getBuilder().setSrcPath(path);
    }
}
