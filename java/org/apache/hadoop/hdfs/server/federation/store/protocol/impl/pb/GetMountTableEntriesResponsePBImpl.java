package org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.GetMountTableEntriesResponseProtoOrBuilder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.MountTableRecordProto;
import org.apache.hadoop.hdfs.server.federation.store.protocol.GetMountTableEntriesResponse;
import org.apache.hadoop.hdfs.server.federation.store.records.MountTable;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.MountTablePBImpl;
import org.apache.hadoop.hdfs.server.federation.store.records.impl.pb.PBRecord;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Protobuf implementation of the state store API object
 * GetMountTableEntriesResponse.
 */
public class GetMountTableEntriesResponsePBImpl
        extends GetMountTableEntriesResponse implements PBRecord {

    private FederationProtocolPBTranslator<GetMountTableEntriesResponseProto,
            GetMountTableEntriesResponseProto.Builder,
            GetMountTableEntriesResponseProtoOrBuilder> translator =
            new FederationProtocolPBTranslator<GetMountTableEntriesResponseProto,
                    GetMountTableEntriesResponseProto.Builder,
                    GetMountTableEntriesResponseProtoOrBuilder>(
                    GetMountTableEntriesResponseProto.class);

    public GetMountTableEntriesResponsePBImpl() {
    }

    public GetMountTableEntriesResponsePBImpl(
            GetMountTableEntriesResponseProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public GetMountTableEntriesResponseProto getProto() {
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
    public List<MountTable> getEntries() throws IOException {
        List<MountTableRecordProto> entries =
                this.translator.getProtoOrBuilder().getEntriesList();
        List<MountTable> ret = new ArrayList<MountTable>();
        for (MountTableRecordProto entry : entries) {
            MountTable record = new MountTablePBImpl(entry);
            ret.add(record);
        }
        return ret;
    }

    @Override
    public void setEntries(List<MountTable> records) throws IOException {
        this.translator.getBuilder().clearEntries();
        for (MountTable entry : records) {
            if (entry instanceof MountTablePBImpl) {
                MountTablePBImpl entryPB = (MountTablePBImpl) entry;
                this.translator.getBuilder().addEntries(entryPB.getProto());
            }
        }
    }

    @Override
    public long getTimestamp() {
        return this.translator.getProtoOrBuilder().getTimestamp();
    }

    @Override
    public void setTimestamp(long time) {
        this.translator.getBuilder().setTimestamp(time);
    }
}