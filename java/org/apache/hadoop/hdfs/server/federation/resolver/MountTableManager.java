package org.apache.hadoop.hdfs.server.federation.resolver;

import org.apache.hadoop.hdfs.server.federation.store.protocol.*;

import java.io.IOException;

// 管理挂载表（增删改查刷）+ getDestination获得匹配的nsIds
public interface MountTableManager {
    
    // 从 ZNode中增加、修改、删除数据, 底层会调用 refreshMountTableEntries
    AddMountTableEntryResponse addMountTableEntry(AddMountTableEntryRequest request) throws IOException;
    UpdateMountTableEntryResponse updateMountTableEntry(UpdateMountTableEntryRequest request) throws IOException;
    RemoveMountTableEntryResponse removeMountTableEntry(RemoveMountTableEntryRequest request) throws IOException;

    // 从缓存中读数据，得到所有的挂载表节点
    GetMountTableEntriesResponse getMountTableEntries(GetMountTableEntriesRequest request) throws IOException;

    // 从 ZNode中重新读取数据刷新到缓存
    RefreshMountTableEntriesResponse refreshMountTableEntries(RefreshMountTableEntriesRequest request) throws IOException;

    // 获得匹配的 ns_ids
    GetDestinationResponse getDestination(GetDestinationRequest request) throws IOException;
}