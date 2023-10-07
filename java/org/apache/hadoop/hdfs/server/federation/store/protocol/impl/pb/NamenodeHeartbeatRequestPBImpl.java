package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeHeartbeatRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeHeartbeatRequestProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeHeartbeatRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeMembershipRecordProto;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.protocol.NamenodeHeartbeatRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.MembershipStatePBImpl;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * NamenodeHeartbeatRequest.
 */
public class NamenodeHeartbeatRequestPBImpl
        extends NamenodeHeartbeatRequest implements PBRecord {

    private FederationProtocolPBTranslator<NamenodeHeartbeatRequestProto, Builder,
            NamenodeHeartbeatRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<NamenodeHeartbeatRequestProto,
                    Builder,
                    NamenodeHeartbeatRequestProtoOrBuilder>(
                    NamenodeHeartbeatRequestProto.class);

    public NamenodeHeartbeatRequestPBImpl() {
    }

    @Override
    public NamenodeHeartbeatRequestProto getProto() {
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
    public MembershipState getNamenodeMembership() throws IOException {
        NamenodeMembershipRecordProto membershipProto =
                this.translator.getProtoOrBuilder().getNamenodeMembership();
        MembershipState membership =
                StateStoreSerializer.newRecord(MembershipState.class);
        if (membership instanceof MembershipStatePBImpl) {
            MembershipStatePBImpl membershipPB = (MembershipStatePBImpl) membership;
            membershipPB.setProto(membershipProto);
            return membershipPB;
        } else {
            throw new IOException("Cannot get membership from request");
        }
    }

    @Override
    public void setNamenodeMembership(MembershipState membership)
            throws IOException {
        if (membership instanceof MembershipStatePBImpl) {
            MembershipStatePBImpl membershipPB = (MembershipStatePBImpl) membership;
            NamenodeMembershipRecordProto membershipProto =
                    (NamenodeMembershipRecordProto) membershipPB.getProto();
            this.translator.getBuilder().setNamenodeMembership(membershipProto);
        } else {
            throw new IOException("Cannot set mount table entry");
        }
    }
}