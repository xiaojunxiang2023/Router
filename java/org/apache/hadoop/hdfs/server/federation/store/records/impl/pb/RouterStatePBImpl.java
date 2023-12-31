package org.apache.hadoop.hdfs.server.federation.store.records.impl.pb;

import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterRecordProto;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterRecordProto.Builder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.RouterRecordProtoOrBuilder;
import org.apache.hadoop.hdfs.federation.protocol.proto.HdfsServerFederationProtos.StateStoreVersionRecordProto;
import org.apache.hadoop.hdfs.server.federation.router.RouterServiceState;
import org.apache.hadoop.hdfs.server.federation.store.driver.StateStoreSerializer;
import org.apache.hadoop.hdfs.server.federation.store.protocol.impl.pb.FederationProtocolPBTranslator;
import org.apache.hadoop.hdfs.server.federation.store.records.RouterState;
import org.apache.hadoop.hdfs.server.federation.store.records.StateStoreVersion;
import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

public class RouterStatePBImpl extends RouterState implements PBRecord {

    // 很多数据都存放在 translator.builder中
    private FederationProtocolPBTranslator<RouterRecordProto, Builder,
            RouterRecordProtoOrBuilder> translator = new FederationProtocolPBTranslator<>(RouterRecordProto.class);

    public RouterStatePBImpl(RouterRecordProto proto) {
        this.translator.setProto(proto);
    }

    @Override
    public RouterRecordProto getProto() {
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
    public void setAddress(String address) {
        RouterRecordProto.Builder builder = this.translator.getBuilder();
        if (address == null) {
            builder.clearAddress();
        } else {
            builder.setAddress(address);
        }
    }

    @Override
    public String getAddress() {
        RouterRecordProtoOrBuilder proto = this.translator.getProtoOrBuilder();
        if (!proto.hasAddress()) {
            return null;
        }
        return proto.getAddress();
    }

    @Override
    public void setStateStoreVersion(StateStoreVersion version) {
        RouterRecordProto.Builder builder = this.translator.getBuilder();
        if (version instanceof StateStoreVersionPBImpl) {
            StateStoreVersionPBImpl versionPB = (StateStoreVersionPBImpl) version;
            StateStoreVersionRecordProto versionProto =
                    (StateStoreVersionRecordProto) versionPB.getProto();
            builder.setStateStoreVersion(versionProto);
        } else {
            builder.clearStateStoreVersion();
        }
    }

    @Override
    public StateStoreVersion getStateStoreVersion() throws IOException {
        RouterRecordProtoOrBuilder proto = this.translator.getProtoOrBuilder();
        if (!proto.hasStateStoreVersion()) {
            return null;
        }
        StateStoreVersionRecordProto versionProto = proto.getStateStoreVersion();
        StateStoreVersion version =
                StateStoreSerializer.newRecord(StateStoreVersion.class);
        if (version instanceof StateStoreVersionPBImpl) {
            StateStoreVersionPBImpl versionPB = (StateStoreVersionPBImpl) version;
            versionPB.setProto(versionProto);
            return versionPB;
        } else {
            throw new IOException("Cannot get State Store version");
        }
    }

    @Override
    public RouterServiceState getStatus() {
        RouterRecordProtoOrBuilder proto = this.translator.getProtoOrBuilder();
        if (!proto.hasStatus()) {
            return null;
        }
        return RouterServiceState.valueOf(proto.getStatus());
    }

    @Override
    public void setStatus(RouterServiceState newStatus) {
        RouterRecordProto.Builder builder = this.translator.getBuilder();
        if (newStatus == null) {
            builder.clearStatus();
        } else {
            builder.setStatus(newStatus.toString());
        }
    }

    @Override
    public String getVersion() {
        RouterRecordProtoOrBuilder proto = this.translator.getProtoOrBuilder();
        if (!proto.hasVersion()) {
            return null;
        }
        return proto.getVersion();
    }

    @Override
    public void setVersion(String version) {
        RouterRecordProto.Builder builder = this.translator.getBuilder();
        if (version == null) {
            builder.clearVersion();
        } else {
            builder.setVersion(version);
        }
    }

    @Override
    public String getCompileInfo() {
        RouterRecordProtoOrBuilder proto = this.translator.getProtoOrBuilder();
        if (!proto.hasCompileInfo()) {
            return null;
        }
        return proto.getCompileInfo();
    }

    @Override
    public void setCompileInfo(String info) {
        RouterRecordProto.Builder builder = this.translator.getBuilder();
        if (info == null) {
            builder.clearCompileInfo();
        } else {
            builder.setCompileInfo(info);
        }
    }

    @Override
    public void setDateStarted(long dateStarted) {
        this.translator.getBuilder().setDateStarted(dateStarted);
    }

    @Override
    public long getDateStarted() {
        return this.translator.getProtoOrBuilder().getDateStarted();
    }

    @Override
    public void setDateModified(long time) {
        if (getStatus() != RouterServiceState.EXPIRED) {
            this.translator.getBuilder().setDateModified(time);
        }
    }

    @Override
    public long getDateModified() {
        return this.translator.getProtoOrBuilder().getDateModified();
    }

    @Override
    public void setDateCreated(long time) {
        this.translator.getBuilder().setDateCreated(time);
    }

    @Override
    public long getDateCreated() {
        return this.translator.getProtoOrBuilder().getDateCreated();
    }

    @Override
    public void setAdminAddress(String adminAddress) {
        this.translator.getBuilder().setAdminAddress(adminAddress);
    }

    @Override
    public String getAdminAddress() {
        return this.translator.getProtoOrBuilder().getAdminAddress();
    }
}
