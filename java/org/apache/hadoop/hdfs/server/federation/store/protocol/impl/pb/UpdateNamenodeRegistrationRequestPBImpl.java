package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateNamenodeRegistrationRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateNamenodeRegistrationRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamenodeServiceState;
import org.apache.hadoop.hdfs.server.federation.store.protocol.UpdateNamenodeRegistrationRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * OverrideNamenodeRegistrationRequest.
 */
public class UpdateNamenodeRegistrationRequestPBImpl
        extends UpdateNamenodeRegistrationRequest implements PBRecord {

    private FederationProtocolPBTranslator<
            UpdateNamenodeRegistrationRequestProto,
            UpdateNamenodeRegistrationRequestProto.Builder,
            UpdateNamenodeRegistrationRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<
                    UpdateNamenodeRegistrationRequestProto,
                    UpdateNamenodeRegistrationRequestProto.Builder,
                    UpdateNamenodeRegistrationRequestProtoOrBuilder>(
                    UpdateNamenodeRegistrationRequestProto.class);

    public UpdateNamenodeRegistrationRequestPBImpl() {
    }

    @Override
    public UpdateNamenodeRegistrationRequestProto getProto() {
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

    @Override
    public String getNameserviceId() {
        return this.translator.getProtoOrBuilder().getNameserviceId();
    }

    @Override
    public String getNamenodeId() {
        return this.translator.getProtoOrBuilder().getNamenodeId();
    }

    @Override
    public FederationNamenodeServiceState getState() {
        return FederationNamenodeServiceState
                .valueOf(this.translator.getProtoOrBuilder().getState());
    }

    @Override
    public void setNameserviceId(String nsId) {
        this.translator.getBuilder().setNameserviceId(nsId);
    }

    @Override
    public void setNamenodeId(String nnId) {
        this.translator.getBuilder().setNamenodeId(nnId);
    }

    @Override
    public void setState(FederationNamenodeServiceState state) {
        this.translator.getBuilder().setState(state.toString());
    }
}