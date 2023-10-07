package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeHeartbeatResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.NamenodeHeartbeatResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.NamenodeHeartbeatResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * NamenodeHeartbeatResponse.
 */
public class NamenodeHeartbeatResponsePBImpl extends NamenodeHeartbeatResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<NamenodeHeartbeatResponseProto,
            NamenodeHeartbeatResponseProto.Builder,
            NamenodeHeartbeatResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<NamenodeHeartbeatResponseProto,
                    NamenodeHeartbeatResponseProto.Builder,
                    NamenodeHeartbeatResponseProtoOrBuilder>(
                    NamenodeHeartbeatResponseProto.class);

    public NamenodeHeartbeatResponsePBImpl() {
    }

    @Override
    public NamenodeHeartbeatResponseProto getProto() {
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