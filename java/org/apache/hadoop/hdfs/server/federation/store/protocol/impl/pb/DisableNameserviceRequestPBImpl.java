package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisableNameserviceRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.DisableNameserviceRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * DisableNameserviceRequest.
 */
public class DisableNameserviceRequestPBImpl extends DisableNameserviceRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<DisableNameserviceRequestProto,
            Builder, DisableNameserviceRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    DisableNameserviceRequestProto.class);

    public DisableNameserviceRequestPBImpl() {
    }

    public DisableNameserviceRequestPBImpl(DisableNameserviceRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public DisableNameserviceRequestProto getProto() {
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
    public String getNameServiceId() {
        return this.translator.getProtoOrBuilder().getNameServiceId();
    }

    @Override
    public void setNameServiceId(String nsId) {
        this.translator.getBuilder().setNameServiceId(nsId);
    }
}
