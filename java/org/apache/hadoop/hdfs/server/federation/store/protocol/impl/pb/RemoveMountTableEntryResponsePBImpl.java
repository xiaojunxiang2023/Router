package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RemoveMountTableEntryResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RemoveMountTableEntryResponse.
 */
public class RemoveMountTableEntryResponsePBImpl
        extends RemoveMountTableEntryResponse implements PBRecord {

    private FederationProtocolPBTranslator<RemoveMountTableEntryResponseProto,
            Builder, RemoveMountTableEntryResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<RemoveMountTableEntryResponseProto,
                    RemoveMountTableEntryResponseProto.Builder,
                    RemoveMountTableEntryResponseProtoOrBuilder>(
                    RemoveMountTableEntryResponseProto.class);

    public RemoveMountTableEntryResponsePBImpl() {
    }

    public RemoveMountTableEntryResponsePBImpl(
            RemoveMountTableEntryResponseProto proto) {
        this.setProto(proto);
    }

    @Override
    public RemoveMountTableEntryResponseProto getProto() {
        return this.translator.build();
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