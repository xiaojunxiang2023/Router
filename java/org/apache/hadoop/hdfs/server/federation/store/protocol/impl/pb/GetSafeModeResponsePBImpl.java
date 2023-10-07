package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetSafeModeResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetSafeModeResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetSafeModeResponse.
 */
public class GetSafeModeResponsePBImpl extends GetSafeModeResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<GetSafeModeResponseProto,
            Builder, GetSafeModeResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    GetSafeModeResponseProto.class);

    public GetSafeModeResponsePBImpl() {
    }

    public GetSafeModeResponsePBImpl(GetSafeModeResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetSafeModeResponseProto getProto() {
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

    @Override
    public boolean isInSafeMode() {
        return this.translator.getProtoOrBuilder().getIsInSafeMode();
    }

    @Override
    public void setSafeMode(boolean isInSafeMode) {
        this.translator.getBuilder().setIsInSafeMode(isInSafeMode);
    }
}
