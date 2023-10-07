package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.FederationNamespaceInfoProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamespaceInfoResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetNamespaceInfoResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.server.federation.resolver.FederationNamespaceInfo;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetNamespaceInfoResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Protobuf implementation of the state store API object
 * GetNamespaceInfoResponse.
 */
public class GetNamespaceInfoResponsePBImpl
        extends GetNamespaceInfoResponse implements PBRecord {

    private FederationProtocolPBTranslator<GetNamespaceInfoResponseProto,
            GetNamespaceInfoResponseProto.Builder,
            GetNamespaceInfoResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<GetNamespaceInfoResponseProto,
                    GetNamespaceInfoResponseProto.Builder,
                    GetNamespaceInfoResponseProtoOrBuilder>(
                    GetNamespaceInfoResponseProto.class);

    public GetNamespaceInfoResponsePBImpl() {
    }

    @Override
    public GetNamespaceInfoResponseProto getProto() {
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
    public Set<FederationNamespaceInfo> getNamespaceInfo() {

        Set<FederationNamespaceInfo> ret = new HashSet<FederationNamespaceInfo>();
        List<FederationNamespaceInfoProto> namespaceList =
                this.translator.getProtoOrBuilder().getNamespaceInfosList();
        for (FederationNamespaceInfoProto ns : namespaceList) {
            FederationNamespaceInfo info = new FederationNamespaceInfo(
                    ns.getBlockPoolId(), ns.getClusterId(), ns.getNameserviceId());
            ret.add(info);
        }
        return ret;
    }

    @Override
    public void setNamespaceInfo(Set<FederationNamespaceInfo> namespaceInfo) {
        int index = 0;
        for (FederationNamespaceInfo item : namespaceInfo) {
            FederationNamespaceInfoProto.Builder itemBuilder =
                    FederationNamespaceInfoProto.newBuilder();
            itemBuilder.setClusterId(item.getClusterId());
            itemBuilder.setBlockPoolId(item.getBlockPoolId());
            itemBuilder.setNameserviceId(item.getNameserviceId());
            this.translator.getBuilder().addNamespaceInfos(index,
                    itemBuilder.build());
            index++;
        }
    }
}