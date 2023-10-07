package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetSafeModeRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetSafeModeRequest.
 */
public class GetSafeModeRequestPBImpl extends GetSafeModeRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<GetSafeModeRequestProto,
            Builder, GetSafeModeRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(GetSafeModeRequestProto.class);

    public GetSafeModeRequestPBImpl() {
    }

    public GetSafeModeRequestPBImpl(GetSafeModeRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetSafeModeRequestProto getProto() {
        return translator.build();
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
