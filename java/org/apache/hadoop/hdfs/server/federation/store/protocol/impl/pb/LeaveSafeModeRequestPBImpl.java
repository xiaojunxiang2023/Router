package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.LeaveSafeModeRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.LeaveSafeModeRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * LeaveSafeModeRequest.
 */
public class LeaveSafeModeRequestPBImpl extends LeaveSafeModeRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<LeaveSafeModeRequestProto,
            Builder, LeaveSafeModeRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(LeaveSafeModeRequestProto.class);

    public LeaveSafeModeRequestPBImpl() {
    }

    public LeaveSafeModeRequestPBImpl(LeaveSafeModeRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public LeaveSafeModeRequestProto getProto() {
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
