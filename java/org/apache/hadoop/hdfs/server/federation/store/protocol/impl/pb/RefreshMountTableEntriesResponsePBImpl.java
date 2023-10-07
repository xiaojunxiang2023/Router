package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RefreshMountTableEntriesResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RefreshMountTableEntriesResponse.
 */
public class RefreshMountTableEntriesResponsePBImpl
        extends RefreshMountTableEntriesResponse implements PBRecord {

    private FederationProtocolPBTranslator<RefreshMountTableEntriesResponseProto,
            Builder, RefreshMountTableEntriesResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    RefreshMountTableEntriesResponseProto.class);

    public RefreshMountTableEntriesResponsePBImpl() {
    }

    public RefreshMountTableEntriesResponsePBImpl(
            RefreshMountTableEntriesResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public RefreshMountTableEntriesResponseProto getProto() {
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
    public boolean getResult() {
        return this.translator.getProtoOrBuilder().getResult();
    }

    ;

    @Override
    public void setResult(boolean result) {
        this.translator.getBuilder().setResult(result);
    }
}