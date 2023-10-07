package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetDestinationResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetDestinationResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Protobuf implementation of the state store API object
 * GetDestinationResponse.
 */
public class GetDestinationResponsePBImpl
        extends GetDestinationResponse implements PBRecord {

    private FederationProtocolPBTranslator<GetDestinationResponseProto,
            Builder, GetDestinationResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    GetDestinationResponseProto.class);

    public GetDestinationResponsePBImpl() {
    }

    public GetDestinationResponsePBImpl(
            GetDestinationResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetDestinationResponseProto getProto() {
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

    @Override
    public Collection<String> getDestinations() {
        return new ArrayList<>(
                this.translator.getProtoOrBuilder().getDestinationsList());
    }

    @Override
    public void setDestinations(Collection<String> nsIds) {
        this.translator.getBuilder().clearDestinations();
        for (String nsId : nsIds) {
            this.translator.getBuilder().addDestinations(nsId);
        }
    }
}