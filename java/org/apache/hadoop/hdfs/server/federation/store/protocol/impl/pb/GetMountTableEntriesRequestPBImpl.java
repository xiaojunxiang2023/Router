package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetMountTableEntriesRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetMountTableEntriesRequest.
 */
public class GetMountTableEntriesRequestPBImpl
        extends GetMountTableEntriesRequest implements PBRecord {

    private FederationProtocolPBTranslator<GetMountTableEntriesRequestProto,
            GetMountTableEntriesRequestProto.Builder,
            GetMountTableEntriesRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<GetMountTableEntriesRequestProto,
                    GetMountTableEntriesRequestProto.Builder,
                    GetMountTableEntriesRequestProtoOrBuilder>(
                    GetMountTableEntriesRequestProto.class);

    public GetMountTableEntriesRequestPBImpl() {
    }

    public GetMountTableEntriesRequestPBImpl(
            GetMountTableEntriesRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetMountTableEntriesRequestProto getProto() {
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