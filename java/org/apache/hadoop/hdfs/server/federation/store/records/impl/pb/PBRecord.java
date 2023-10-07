package org.apache.hadoop.hdfs.server.federation.store.records.impl.pb;

import org.apache.hadoop.thirdparty.protobuf.Message;

import java.io.IOException;

// 所有 RequestProto/ResponseProto 实例的父接口
public interface PBRecord {

    Message getProto();

    void setProto(Message proto);

    // 从序列化编码后的数据中读取
    void readInstance(String base64String) throws IOException;
}
