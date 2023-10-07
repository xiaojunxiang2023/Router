package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.EnterSafeModeResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * EnterSafeModeResponse.
 */
public class EnterSafeModeResponsePBImpl extends EnterSafeModeResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<EnterSafeModeResponseProto,
            Builder, EnterSafeModeResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    EnterSafeModeResponseProto.class);

    public EnterSafeModeResponsePBImpl() {
    }

    public EnterSafeModeResponsePBImpl(EnterSafeModeResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public EnterSafeModeResponseProto getProto() {
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
    public boolean getStatus() {
        return this.translator.getProtoOrBuilder().getStatus();
    }

    @Override
    public void setStatus(boolean result) {
        this.translator.getBuilder().setStatus(result);
    }
}
