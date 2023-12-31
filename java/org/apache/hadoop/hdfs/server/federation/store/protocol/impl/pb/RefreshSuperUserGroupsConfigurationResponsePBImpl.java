package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RefreshSuperUserGroupsConfigurationResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RefreshSuperUserGroupsConfigurationResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RefreshSuperUserGroupsConfigurationResponse.
 */
public class RefreshSuperUserGroupsConfigurationResponsePBImpl
        extends RefreshSuperUserGroupsConfigurationResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<
            RefreshSuperUserGroupsConfigurationResponseProto,
            Builder,
            RefreshSuperUserGroupsConfigurationResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<>(
                    RefreshSuperUserGroupsConfigurationResponseProto.class);

    public RefreshSuperUserGroupsConfigurationResponsePBImpl() {
    }

    public RefreshSuperUserGroupsConfigurationResponsePBImpl(
            RefreshSuperUserGroupsConfigurationResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public RefreshSuperUserGroupsConfigurationResponseProto getProto() {
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

    @Override
    public boolean getStatus() {
        return this.translator.getProtoOrBuilder().getStatus();
    }

    @Override
    public void setStatus(boolean result) {
        this.translator.getBuilder().setStatus(result);
    }
}
