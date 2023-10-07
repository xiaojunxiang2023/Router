package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetDisabledNameservicesRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetDisabledNameservicesRequest.
 */
public class GetDisabledNameservicesRequestPBImpl
        extends GetDisabledNameservicesRequest implements PBRecord {

    private FederationProtocolPBTranslator<GetDisabledNameservicesRequestProto,
            Builder, GetDisabledNameservicesRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    GetDisabledNameservicesRequestProto.class);

    public GetDisabledNameservicesRequestPBImpl() {
        // As this request has no parameter, we need to initialize it
        this.translator.getBuilder();
    }

    public GetDisabledNameservicesRequestPBImpl(
            GetDisabledNameservicesRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetDisabledNameservicesRequestProto getProto() {
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
}
