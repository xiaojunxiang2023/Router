package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamenodeRegistrationsRequestProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamenodeRegistrationsRequestProtoOrBuilder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeMembershipRecordProto;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamenodeRegistrationsRequest;
import org.apache.hadoop.hdfs.server.federation.store.records.MembershipState;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.MembershipStatePBImpl;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * GetNamenodeRegistrationsRequest.
 */
public class GetNamenodeRegistrationsRequestPBImpl
        extends GetNamenodeRegistrationsRequest implements PBRecord {

    private FederationProtocolPBTranslator<GetNamenodeRegistrationsRequestProto,
            GetNamenodeRegistrationsRequestProto.Builder,
            GetNamenodeRegistrationsRequestProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<
                    GetNamenodeRegistrationsRequestProto,
                    GetNamenodeRegistrationsRequestProto.Builder,
                    GetNamenodeRegistrationsRequestProtoOrBuilder>(
                    GetNamenodeRegistrationsRequestProto.class);

    public GetNamenodeRegistrationsRequestPBImpl() {
    }

    public GetNamenodeRegistrationsRequestPBImpl(
            GetNamenodeRegistrationsRequestProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetNamenodeRegistrationsRequestProto getProto() {
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
    public MembershipState getPartialMembership() {
        GetNamenodeRegistrationsRequestProtoOrBuilder proto =
                this.translator.getProtoOrBuilder();
        if (!proto.hasMembership()) {
            return null;
        }
        NamenodeMembershipRecordProto memberProto = proto.getMembership();
        return new MembershipStatePBImpl(memberProto);
    }

    @Override
    public void setPartialMembership(MembershipState member) {
        MembershipStatePBImpl memberPB = (MembershipStatePBImpl) member;
        this.translator.getBuilder().setMembership(memberPB.getProto());
    }
}
