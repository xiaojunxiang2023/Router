package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.EnableNameserviceResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.EnableNameserviceResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * EnableNameserviceResponse.
 */
public class EnableNameserviceResponsePBImpl extends EnableNameserviceResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<EnableNameserviceResponseProto,
            Builder, EnableNameserviceResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    EnableNameserviceResponseProto.class);

    public EnableNameserviceResponsePBImpl() {
    }

    public EnableNameserviceResponsePBImpl(EnableNameserviceResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public EnableNameserviceResponseProto getProto() {
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
