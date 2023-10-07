package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnterSafeModeRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetRouterRegistrationResponseProto.Builder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.EnterSafeModeRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * EnterSafeModeRequest.
 */
public class EnterSafeModeRequestPBImpl extends EnterSafeModeRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<EnterSafeModeRequestProto,
            Builder, EnterSafeModeRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(EnterSafeModeRequestProto.class);

    public EnterSafeModeRequestPBImpl() {
    }

    public EnterSafeModeRequestPBImpl(EnterSafeModeRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public EnterSafeModeRequestProto getProto() {
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
