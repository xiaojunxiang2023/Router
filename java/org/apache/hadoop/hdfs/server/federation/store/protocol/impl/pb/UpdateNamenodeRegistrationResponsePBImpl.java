package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateNamenodeRegistrationResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.UpdateNamenodeRegistrationResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.UpdateNamenodeRegistrationResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * OverrideNamenodeRegistrationResponse.
 */
public class UpdateNamenodeRegistrationResponsePBImpl
        extends UpdateNamenodeRegistrationResponse implements PBRecord {

    private FederationProtocolPBTranslator<
            UpdateNamenodeRegistrationResponseProto,
            UpdateNamenodeRegistrationResponseProto.Builder,
            UpdateNamenodeRegistrationResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<
                    UpdateNamenodeRegistrationResponseProto,
                    UpdateNamenodeRegistrationResponseProto.Builder,
                    UpdateNamenodeRegistrationResponseProtoOrBuilder>(
                    UpdateNamenodeRegistrationResponseProto.class);

    public UpdateNamenodeRegistrationResponsePBImpl() {
    }

    @Override
    public UpdateNamenodeRegistrationResponseProto getProto() {
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
        return this.translator.getProtoOrBuilder().getStatus();
    }

    @Override
    public void setResult(boolean result) {
        this.translator.getBuilder().setStatus(result);
    }
}