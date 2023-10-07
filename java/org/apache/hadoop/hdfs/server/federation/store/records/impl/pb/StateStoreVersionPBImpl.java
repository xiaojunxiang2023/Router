package org.apache.hadoop.hdfs.server.federation.store.records.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.StateStoreVersionRecordProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.StateStoreVersionRecordProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.StateStoreVersionRecordProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb.FederationProtocolPBTranslator;
import org.apache.hadoop.hdfs.server.federation.store.records.StateStoreVersion;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the StateStoreVersion record.
 */
public class StateStoreVersionPBImpl extends StateStoreVersion
        implements PBRecord {

    private FederationProtocolPBTranslator<StateStoreVersionRecordProto, Builder,
            StateStoreVersionRecordProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<StateStoreVersionRecordProto,
                    Builder, StateStoreVersionRecordProtoOrBuilder>(
                    StateStoreVersionRecordProto.class);

    public StateStoreVersionPBImpl() {
    }

    @Override
    public StateStoreVersionRecordProto getProto() {
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
    public long getMembershipVersion() {
        return this.translator.getProtoOrBuilder().getMembershipVersion();
    }

    @Override
    public void setMembershipVersion(long version) {
        this.translator.getBuilder().setMembershipVersion(version);
    }

    @Override
    public long getMountTableVersion() {
        return this.translator.getProtoOrBuilder().getMountTableVersion();
    }

    @Override
    public void setMountTableVersion(long version) {
        this.translator.getBuilder().setMountTableVersion(version);
    }
}
