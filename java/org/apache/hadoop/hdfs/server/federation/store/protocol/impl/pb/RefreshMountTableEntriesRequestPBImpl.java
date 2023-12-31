package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshMountTableEntriesRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RefreshMountTableEntriesRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RefreshMountTableEntriesRequest.
 */
public class RefreshMountTableEntriesRequestPBImpl
        extends RefreshMountTableEntriesRequest implements PBRecord {

    private FederationProtocolPBTranslator<RefreshMountTableEntriesRequestProto,
            Builder, RefreshMountTableEntriesRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    RefreshMountTableEntriesRequestProto.class);

    public RefreshMountTableEntriesRequestPBImpl() {
    }

    public RefreshMountTableEntriesRequestPBImpl(
            RefreshMountTableEntriesRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public RefreshMountTableEntriesRequestProto getProto() {
        // if builder is null build() returns null, calling getBuilder() to
        // instantiate builder
        this.translator.getBuilder();
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
}