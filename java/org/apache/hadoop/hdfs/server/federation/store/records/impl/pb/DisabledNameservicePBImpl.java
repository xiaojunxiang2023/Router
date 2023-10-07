package org.apache.hadoop.hdfs.server.federation.store.records.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisabledNameserviceRecordProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisabledNameserviceRecordProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.DisabledNameserviceRecordProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb.FederationProtocolPBTranslator;
import org.apache.hadoop.hdfs.server.federation.store.records.DisabledNameservice;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the {@link DisabledNameservice} record.
 */
public class DisabledNameservicePBImpl extends DisabledNameservice
        implements PBRecord {

    private FederationProtocolPBTranslator<DisabledNameserviceRecordProto,
            Builder, DisabledNameserviceRecordProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<
                    DisabledNameserviceRecordProto, Builder,
                    DisabledNameserviceRecordProtoOrBuilder>(
                    DisabledNameserviceRecordProto.class);

    public DisabledNameservicePBImpl() {
    }

    public DisabledNameservicePBImpl(
            DisabledNameserviceRecordProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public DisabledNameserviceRecordProto getProto() {
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
    public String getNameserviceId() {
        return this.translator.getProtoOrBuilder().getNameServiceId();
    }

    @Override
    public void setNameserviceId(String nameServiceId) {
        this.translator.getBuilder().setNameServiceId(nameServiceId);
    }

    @Override
    public void setDateModified(long time) {
        this.translator.getBuilder().setDateModified(time);
    }

    @Override
    public long getDateModified() {
        return this.translator.getProtoOrBuilder().getDateModified();
    }

    @Override
    public void setDateCreated(long time) {
        this.translator.getBuilder().setDateCreated(time);
    }

    @Override
    public long getDateCreated() {
        return this.translator.getProtoOrBuilder().getDateCreated();
    }
}
