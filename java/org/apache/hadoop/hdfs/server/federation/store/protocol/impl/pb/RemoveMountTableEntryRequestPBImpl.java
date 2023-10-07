package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RemoveMountTableEntryRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RemoveMountTableEntryRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RemoveMountTableEntryRequest.
 */
public class RemoveMountTableEntryRequestPBImpl
        extends RemoveMountTableEntryRequest implements PBRecord {

    private FederationProtocolPBTranslator<RemoveMountTableEntryRequestProto,
            RemoveMountTableEntryRequestProto.Builder,
            RemoveMountTableEntryRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<RemoveMountTableEntryRequestProto,
                    RemoveMountTableEntryRequestProto.Builder,
                    RemoveMountTableEntryRequestProtoOrBuilder>(
                    RemoveMountTableEntryRequestProto.class);

    public RemoveMountTableEntryRequestPBImpl() {
    }

    public RemoveMountTableEntryRequestPBImpl(
            RemoveMountTableEntryRequestProto proto) {
        this.setProto(proto);
    }

    @Override
    public RemoveMountTableEntryRequestProto getProto() {
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
    public String getSrcPath() {
        return this.translator.getProtoOrBuilder().getSrcPath();
    }

    @Override
    public void setSrcPath(String path) {
        this.translator.getBuilder().setSrcPath(path);
    }
}