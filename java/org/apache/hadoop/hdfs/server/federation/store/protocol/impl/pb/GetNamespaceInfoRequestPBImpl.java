package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamespaceInfoRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamespaceInfoRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamespaceInfoRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamespaceInfoRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetNamespaceInfoRequest.
 */
public class GetNamespaceInfoRequestPBImpl extends GetNamespaceInfoRequest
        implements PBRecord {

    private FederationProtocolPBTranslator<GetNamespaceInfoRequestProto,
            Builder, GetNamespaceInfoRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<GetNamespaceInfoRequestProto,
                    Builder, GetNamespaceInfoRequestProtoOrBuilder>(
                    GetNamespaceInfoRequestProto.class);

    public GetNamespaceInfoRequestPBImpl() {
    }

    @Override
    public GetNamespaceInfoRequestProto getProto() {
        return this.translator.build();
    }

    @Override
    public void setProto(Message protocol) {
        this.translator.setProto(protocol);
    }

    @Override
    public void readInstance(String base64String) throws IOException {
        this.translator.readInstance(base64String);
    }
}