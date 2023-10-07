package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.EnableNameserviceRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * EnableNameserviceRequest.
 */
public class EnableNameserviceRequestPBImpl extends EnableNameserviceRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<EnableNameserviceRequestProto,
            Builder, EnableNameserviceRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    EnableNameserviceRequestProto.class);

    public EnableNameserviceRequestPBImpl() {
    }

    public EnableNameserviceRequestPBImpl(EnableNameserviceRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public EnableNameserviceRequestProto getProto() {
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
