package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.LeaveSafeModeResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * LeaveSafeModeResponse.
 */
public class LeaveSafeModeResponsePBImpl extends LeaveSafeModeResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<LeaveSafeModeResponseProto,
            Builder, LeaveSafeModeResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    LeaveSafeModeResponseProto.class);

    public LeaveSafeModeResponsePBImpl() {
    }

    public LeaveSafeModeResponsePBImpl(LeaveSafeModeResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public LeaveSafeModeResponseProto getProto() {
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
