package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDisabledNameservicesResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetDisabledNameservicesResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Protobuf implementation of the state store API object
 * GetDisabledNameservicesResponse.
 */
public class GetDisabledNameservicesResponsePBImpl
        extends GetDisabledNameservicesResponse implements PBRecord {

    private FederationProtocolPBTranslator<GetDisabledNameservicesResponseProto,
            Builder, GetDisabledNameservicesResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<
                    GetDisabledNameservicesResponseProto, Builder,
                    GetDisabledNameservicesResponseProtoOrBuilder>(
                    GetDisabledNameservicesResponseProto.class);

    public GetDisabledNameservicesResponsePBImpl() {
    }

    public GetDisabledNameservicesResponsePBImpl(
            GetDisabledNameservicesResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetDisabledNameservicesResponseProto getProto() {
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
    public Set<String> getNameservices() {
        List<String> nsIds =
                this.translator.getProtoOrBuilder().getNameServiceIdsList();
        return new TreeSet<>(nsIds);
    }

    @Override
    public void setNameservices(Set<String> nameservices) {
        this.translator.getBuilder().clearNameServiceIds();
        for (String nsId : nameservices) {
            this.translator.getBuilder().addNameServiceIds(nsId);
        }
    }
}
