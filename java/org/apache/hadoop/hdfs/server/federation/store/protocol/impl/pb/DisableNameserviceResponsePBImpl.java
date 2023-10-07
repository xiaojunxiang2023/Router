package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.DisableNameserviceResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * {@link DisableNameserviceResponse}.
 */
public class DisableNameserviceResponsePBImpl
        extends DisableNameserviceResponse implements PBRecord {

    private FederationProtocolPBTranslator<DisableNameserviceResponseProto,
            Builder, DisableNameserviceResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    DisableNameserviceResponseProto.class);

    public DisableNameserviceResponsePBImpl() {
    }

    public DisableNameserviceResponsePBImpl(
            DisableNameserviceResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public DisableNameserviceResponseProto getProto() {
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
    public void setStatus(boolean status) {
        this.translator.getBuilder().setStatus(status);
    }
}