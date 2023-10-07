package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterHeartbeatResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterHeartbeatResponseProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterHeartbeatResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.store.protocol.RouterHeartbeatResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

/**
 * Protobuf implementation of the state store API object
 * RouterHeartbeatResponse.
 */
public class RouterHeartbeatResponsePBImpl extends RouterHeartbeatResponse
        implements PBRecord {

    private FederationProtocolPBTranslator<RouterHeartbeatResponseProto, Builder,
            RouterHeartbeatResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<RouterHeartbeatResponseProto,
                    Builder, RouterHeartbeatResponseProtoOrBuilder>(
                    RouterHeartbeatResponseProto.class);

    public RouterHeartbeatResponsePBImpl() {
    }

    @Override
    public RouterHeartbeatResponseProto getProto() {
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
    public boolean getStatus() {
        return this.translator.getProtoOrBuilder().getStatus();
    }

    @Override
    public void setStatus(boolean result) {
        this.translator.getBuilder().setStatus(result);
    }
}
